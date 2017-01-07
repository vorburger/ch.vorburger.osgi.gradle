package ch.vorburger.osgi.gradle.internal;

import java.io.File;
import java.util.concurrent.Future;

/**
 * Builds source code (using Gradle).
 *
 * @author Michael Vorburger
 */
public interface BuildService extends AutoCloseable {

    Future<?> build(File projectDirectory, String... tasks);

    Future<?> buildContinously(File projectDirectory, String task, BuildServiceListener listener);

    default Future<?> buildContinously(File projectDirectory, String task, BuildServiceSingleFileOutputListener listener) {
        return buildContinously(projectDirectory, task, new BuildServiceListenerAdapter(projectDirectory, listener));
    }

}
