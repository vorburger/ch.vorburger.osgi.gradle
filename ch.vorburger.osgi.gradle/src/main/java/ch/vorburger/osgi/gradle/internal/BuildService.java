package ch.vorburger.osgi.gradle.internal;

import java.io.File;
import java.util.concurrent.Future;

/**
 * Builds source code (using Gradle).
 *
 * @author Michael Vorburger
 */
public interface BuildService {

    Future<?> build(File projectDirectory, String... tasks);

    Future<?> buildContinously(File projectDirectory, String task, BuildServiceListener listener);

}
