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
package ch.vorburger.osgi.maven.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import ch.vorburger.osgi.gradle.internal.BuildService;
import ch.vorburger.osgi.gradle.internal.BuildServiceListener;
import ch.vorburger.osgi.gradle.internal.concurrent.ExecutorServiceProvider;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Implementation of BuildService for Maven.
 */
public class MavenBuildService implements BuildService {

    private static final Logger LOG = LoggerFactory.getLogger(MavenBuildService.class);

    private final ListeningExecutorService executorService;
    private WatchService watcher;

    public MavenBuildService() {
        this(ExecutorServiceProvider.newCachedThreadPool(LOG, "MavenBuildService"));
    }

    public MavenBuildService(ListeningExecutorService executorService) {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            throw new RuntimeException("could not create watch service", ex);
        }
        this.executorService = executorService;
    }

    private ListenableFuture<Void> build(File projectDirectory, String[] tasks, boolean continuous, BuildServiceListener listener) {
        return executorService.submit(() -> {
            build(projectDirectory, tasks, listener);
            if (continuous) {
                Path path = projectDirectory.toPath();
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path fileVisitor, BasicFileAttributes attrs) throws IOException {
                        if ("target".equals(fileVisitor.getFileName().toString())) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        fileVisitor.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
                        return FileVisitResult.CONTINUE;
                    }
                });

                while (true) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException x) {
                        return null;
                    }

                    for (WatchEvent<?> event: key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW) {
                            continue;
                        }

                        build(projectDirectory, tasks, listener);
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }

            return null;
        });
    }

    private void build(File projectDirectory, String[] tasks, BuildServiceListener listener) throws MavenInvocationException {
        Logger logger = LoggerFactory.getLogger(getClass().getSimpleName() + " (" + projectDirectory.toString() + ")");

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(projectDirectory, "pom.xml"));
        request.setGoals(Arrays.asList(tasks));

        Invoker invoker = new DefaultInvoker();
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome == null) {
            File mavenCommand = searchPath(System.getenv("PATH"), "mvn");
            invoker.setMavenHome(getMavenHome(mavenCommand));
        } else {
            invoker.setMavenHome(new File(mavenHome));
        }
        invoker.setOutputHandler(logger::info);
        invoker.setErrorHandler(logger::error);
        invoker.execute(request);
        listener.buildSucceeded();
    }

    /**
     * maven command is located in something like /programs/maven/bin/mvn so ../.. makes it maven home
     * @param mavenCommand the location of the maven command
     * @return the maven home
     */
    private File getMavenHome(File mavenCommand) {
        return mavenCommand != null ? mavenCommand.getParentFile().getParentFile() : null;
    }

    private static File searchPath(final String path, final String... lookFor) {
        if (path == null) {
            return null;
        }

        for (final String p : path.split(File.pathSeparator)) {
            for (String command : lookFor) {
                final File e = new File(p, command);
                if (e.isFile()) {
                    return e.getAbsoluteFile();
                }
            }
        }
        return null;
    }

    @Override
    public ListenableFuture<Void> build(File projectDirectory, String... tasks) {
        return build(projectDirectory, tasks, false, () -> {});
    }

    @Override
    public ListenableFuture<Void> buildContinously(File projectDirectory, String task, BuildServiceListener listener) {
        return build(projectDirectory, new String[] { task }, true, listener);
    }

    @Override
    public void close() throws Exception {
        executorService.shutdownNow();
    }

}
