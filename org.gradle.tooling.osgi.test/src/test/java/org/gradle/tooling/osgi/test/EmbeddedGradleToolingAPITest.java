package org.gradle.tooling.osgi.test;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.bundle;

import java.io.File;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

@RunWith(PaxExam.class)
public class EmbeddedGradleToolingAPITest {

    @Configuration
    public Option[] config() {
        return options(
                bundle("file:../org.gradle.tooling.osgi/build/libs/org.gradle.tooling.osgi-1.0.0-SNAPSHOT.jar"),
                junitBundles());
    }

    @Test
    public void gradleHelp() {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File("."));
        ProjectConnection connection = connector.connect();
        try {
            BuildLauncher launcher = connection.newBuild();
            launcher.forTasks("help");
            launcher.setStandardOutput(System.out);
            launcher.setStandardError(System.err);
            launcher.run();
        } finally {
            connection.close();
        }
    }

}
