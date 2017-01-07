package ch.vorburger.osgi.gradle;

import java.io.File;
import java.util.concurrent.Future;
import org.osgi.framework.Bundle;

/**
 * Install a Bundle from source into OSGi framework (builds it).
 *
 * @author Michael Vorburger
 */
public interface SourceInstallService {

    /**
     * Installs an OSGi bundle from a source code project directory.
     *
     * The bundle will NOT be started by this method, yet.
     *
     * @param projectDirectory location of a Gradle buildable project
     * @return a Future of the installed Bundle
     */
    Future<Bundle> installSourceBundle(File projectDirectory);

}
