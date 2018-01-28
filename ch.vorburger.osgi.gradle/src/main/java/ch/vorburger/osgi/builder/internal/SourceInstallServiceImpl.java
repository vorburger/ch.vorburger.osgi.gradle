/**
 * ch.vorburger.osgi.gradle
 *
 * Copyright (C) 2016 - 2017 Michael Vorburger.ch <mike@vorburger.ch>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.vorburger.osgi.builder.internal;

import ch.vorburger.osgi.builder.gradle.internal.GradleBuildService;
import ch.vorburger.osgi.builder.maven.internal.MavenBuildService;
import ch.vorburger.fswatch.DirectoryWatcher;
import ch.vorburger.fswatch.FileWatcherBuilder;
import ch.vorburger.osgi.builder.SourceInstallService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of SourceInstallService.
 *
 * @author Michael Vorburger
 */
// TODO @Component
public class SourceInstallServiceImpl implements SourceInstallService, ch.vorburger.osgi.gradle.SourceInstallService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SourceInstallServiceImpl.class);

    private final BundleContext bundleContext;
    private final BuildService gradleBuildService = new GradleBuildService();
    private final BuildService mavenBuildService = new MavenBuildService();

    private DirectoryWatcher bundleFileWatcher;

    public SourceInstallServiceImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public ListenableFuture<Bundle> installSourceBundle(File projectDirectoryOrBundleJAR) {
        SettableFuture<Bundle> installFuture = SettableFuture.create();
        BuildServiceSingleFileOutputListener listener = singleProducedFile -> {
            try (InputStream inputStream = new FileInputStream(singleProducedFile)) {
                String location = getBundleLocation(projectDirectoryOrBundleJAR);
                Bundle bundle = bundleContext.getBundle(location);
                if (bundle == null) {
                    LOG.info("Installing Bundle from {}", location);
                    bundle = bundleContext.installBundle(location, inputStream);
                    bundle.start();
                } else {
                    LOG.info("Updating Bundle from {}", location);
                    bundle.update(inputStream);
                    // We (possibly "re")-start here, because it's possible that
                    // an initial (or previous) start() failed due to some bug in the bundle
                    // and that could have meanwhile be fixed, but OSGi won't re-try starting
                    // a bundle an update if we don't tell it to...
                    bundle.start();
                }
                installFuture.set(bundle);
            } catch (BundleException | IOException e) {
                LOG.error("Problem reading/installing bundle JAR: {}", singleProducedFile, e);
                installFuture.setException(e);
            }
        };

        ListenableFuture<Void> buildFuture;
        if (new File(projectDirectoryOrBundleJAR, "pom.xml").exists()) {
            LOG.info("Found a POM in directory, will continously build with Maven: {}", projectDirectoryOrBundleJAR);
            buildFuture = mavenBuildService.buildContinously(projectDirectoryOrBundleJAR, "install", listener);
        } else if (projectDirectoryOrBundleJAR.isDirectory()) {
            LOG.info("Found directory (but no POM), will continously build with Gradle: {}", projectDirectoryOrBundleJAR);
            buildFuture = gradleBuildService.buildContinously(projectDirectoryOrBundleJAR, "build", listener);
        } else if (projectDirectoryOrBundleJAR.isFile() && projectDirectoryOrBundleJAR.getName().endsWith(".jar")) {
            LOG.info("Found JAR, will install and update on update: {}", projectDirectoryOrBundleJAR);
            // The JAR is already ready now, and can be started by caller:
            try {
                // NB: The default quietPeriod of 100ms is often not enough while Gradle updates the JAR and leads to ZipException, so 500ms:
                bundleFileWatcher = new FileWatcherBuilder().path(projectDirectoryOrBundleJAR).quietPeriodInMS(500).listener((path, changeKind) -> {
                    switch (changeKind) {
                    case MODIFIED:
                        // NB: FileWatcherBuilder invoked the listener once on start, and then on subsequent changes
                        listener.buildSucceeded(projectDirectoryOrBundleJAR);
                        break;

                    case DELETED:
                        String location = getBundleLocation(projectDirectoryOrBundleJAR);
                        LOG.info("Uninstalling Bundle from {}", location);
                        bundleContext.getBundle(location).uninstall();
                        break;

                    default:
                        LOG.error("Unsupported file watcher change kind, ignored: {}", changeKind);
                        break;
                    }

                    System.out.println(changeKind.name() + " " + path.toString());
                }).build();
                buildFuture = Futures.immediateFuture(null);
            } catch (IOException e) {
                buildFuture = Futures.immediateFailedFuture(e);
            }
            // But we make sure than upon changes it gets reloaded:
            // TODO!!!!
        } else {
            buildFuture = Futures.immediateFailedFuture(
                    new IllegalArgumentException("Neither a directory (with or w.o. pom.xml) nor a JAR, "
                            + "how I am supposed to (build and) install this as an OSGi bundle: "
                            + projectDirectoryOrBundleJAR));
        }

        Futures.addCallback(buildFuture, new FutureCallback<Void>() {

            @Override
            public void onFailure(Throwable throwable) {
                // If this happens, then the listener above will never get invoked
                // because the (first, as it's continous) build failed before, so:
                installFuture.setException(throwable);
            }

            @Override
            public void onSuccess(Void nothing) {
            }
        });
        return installFuture;
    }

    private String getBundleLocation(File projectDirectoryOrBundleJAR) {
        String location = projectDirectoryOrBundleJAR.toURI().toString();
        if (projectDirectoryOrBundleJAR.isDirectory()) {
            location = "source:" + location;
        }
        return location;
    }

    @Override
    // TODO @Deactivate
    public void close() throws Exception {
        gradleBuildService.close();
        mavenBuildService.close();
        if (bundleFileWatcher != null) {
            bundleFileWatcher.close();
        }
    }

}
