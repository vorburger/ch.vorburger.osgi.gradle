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
package ch.vorburger.osgi.builder.gradle.internal;

import ch.vorburger.osgi.builder.internal.BuildService;
import ch.vorburger.osgi.builder.internal.BuildServiceListener;
import ch.vorburger.osgi.builder.internal.ExecutorServiceProvider;
import ch.vorburger.osgi.builder.internal.LoggingOutputStream;
import ch.vorburger.osgi.builder.internal.LoggingOutputStream.Level;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.File;
import java.util.Arrays;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of BuildService for Gradle.
 *
 * @author Michael Vorburger
 */
public class GradleBuildService implements BuildService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(GradleBuildService.class);

    private final ListeningExecutorService executorService;

    public GradleBuildService() {
        this(ExecutorServiceProvider.newCachedThreadPool(LOG, "GradleBuildService"));
    }

    public GradleBuildService(ListeningExecutorService executorService) {
        this.executorService = executorService;
    }

    private ListenableFuture<Void> build(File projectDirectory, String[] tasks, boolean continuous, BuildServiceListener listener) {
        Logger logger = LoggerFactory.getLogger(getClass().getSimpleName() + " (" + projectDirectory.toString() + ")");
        ListenableFuture<Void> future = executorService.submit(() -> {
            ProjectConnection connection = GradleConnector.newConnector()
                    .forProjectDirectory(projectDirectory)
                    .connect();
            try {
                BuildLauncher launcher = connection.newBuild();
                launcher.forTasks(tasks);
                if (continuous) {
                    launcher.withArguments("--continuous");
                }
                launcher.setStandardOutput(new LoggingOutputStream(logger, Level.INFO));
                launcher.setStandardError(new LoggingOutputStream(logger, Level.ERROR));
                launcher.addProgressListener((ProgressListener) event -> {
                    if ("Run build succeeded".equals(event.getDisplayName())) {
                        // or "Run tasks succeeded" ?
                        listener.buildSucceeded();
                    }
                });

                launcher.run();
                return null;
            } finally {
                if (!executorService.isShutdown()) {
                    connection.close();
                    logger.info("ProjectConnection closed");
                }
            }
        });
        logger.info("Tasks submitted to ExecutorService: {}", Arrays.toString(tasks));
        return future;
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
