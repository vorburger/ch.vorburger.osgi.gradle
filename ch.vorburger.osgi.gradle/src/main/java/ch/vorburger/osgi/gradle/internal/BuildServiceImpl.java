package ch.vorburger.osgi.gradle.internal;

import ch.vorburger.osgi.gradle.internal.LoggingOutputStream.Level;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of BuildService.
 *
 * @author Michael Vorburger
 */
public class BuildServiceImpl implements BuildService {
    // TODO implements AutoCloseable close() ?

    private static final Logger LOG = LoggerFactory.getLogger(BuildServiceImpl.class);

    private final ExecutorService executorService;

    public BuildServiceImpl() {
        this(Executors.unconfigurableExecutorService(Executors.newCachedThreadPool()));
    }

    public BuildServiceImpl(ExecutorService executorService) {
        this.executorService = executorService;
    }

    private Future<?> build(File projectDirectory, String[] tasks, boolean continuous, BuildServiceListener listener) {
        Logger logger = LoggerFactory.getLogger(projectDirectory.toString());
        Future<?> future = executorService.submit(() -> {
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
/*
                launcher.run(new ResultHandler<Void>() {
                    @Override
                    public void onComplete(Void result) {
                        logger.info("onComplete()");
                    }

                    @Override
                    public void onFailure(GradleConnectionException failure) {
                        logger.error("onFailure()", failure);
                    }});
*/
                launcher.run();
            } finally {
                logger.info("ProjectConnection closed");
                connection.close();
            }
        });
        logger.info("Tasks submitted to ExecutorService: {}", Arrays.toString(tasks));
        return future;
    }

    @Override
    public Future<?> build(File projectDirectory, String... tasks) {
        return build(projectDirectory, tasks, false, () -> {});
    }

    @Override
    public Future<?> buildContinously(File projectDirectory, String task, BuildServiceListener listener) {
        return build(projectDirectory, new String[] { task }, true, listener);
    }

}
