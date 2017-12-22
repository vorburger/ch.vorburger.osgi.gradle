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

    public SourceInstallServiceImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public ListenableFuture<Bundle> installSourceBundle(File projectDirectory) {
        SettableFuture<Bundle> installFuture = SettableFuture.create();
        BuildServiceSingleFileOutputListener listener = singleProducedFile -> {
            try (InputStream inputStream = new FileInputStream(singleProducedFile)) {
                String location = "source:" + projectDirectory.toURI().toString();
                Bundle bundle = bundleContext.getBundle(location);
                if (bundle == null) {
                    bundle = bundleContext.installBundle(location, inputStream);
                } else {
                    bundle.update(inputStream);
                }
                installFuture.set(bundle);
            } catch (BundleException | IOException e) {
                LOG.error("Problem reading/installing bundle JAR built from source: {}", singleProducedFile, e);
                installFuture.setException(e);
            }
        };

        ListenableFuture<Void> buildFuture;
        if (new File(projectDirectory, "pom.xml").exists()) {
            buildFuture = mavenBuildService.buildContinously(projectDirectory, "install", listener);
        } else {
            buildFuture = gradleBuildService.buildContinously(projectDirectory, "build", listener);
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

    @Override
    // TODO @Deactivate
    public void close() throws Exception {
        gradleBuildService.close();
        mavenBuildService.close();
    }

}
