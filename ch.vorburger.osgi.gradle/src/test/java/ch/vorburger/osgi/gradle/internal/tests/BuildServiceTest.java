package ch.vorburger.osgi.gradle.internal.tests;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import ch.vorburger.osgi.gradle.internal.BuildService;
import ch.vorburger.osgi.gradle.internal.BuildServiceImpl;
import ch.vorburger.osgi.gradle.internal.BuildServiceListener;
import java.io.File;
import java.util.concurrent.Future;
import org.junit.Test;

/**
 * BuildService Test (non-OSGi).
 *
 * @author Michael Vorburger
 */
public class BuildServiceTest {

    File testProjectDirectory = new File("../ch.vorburger.osgi.gradle.test.bundle.provider");

    @Test
    public void help() throws Exception {
        try (BuildService buildService = new BuildServiceImpl()) {
            Future<?> future = buildService.build(testProjectDirectory, "help");
            try {
                future.get(30, SECONDS);
            } finally {
                future.cancel(true);
            }
        }
    }

    @Test
    public void build() throws Exception {
        try (BuildService buildService = new BuildServiceImpl()) {
            Future<?> future = buildService.build(testProjectDirectory, "build");
            try {
                future.get(30, SECONDS);
            } finally {
                future.cancel(true);
            }
        }
    }

    @Test
    public void buildContinously() throws Exception {
        try (BuildService buildService = new BuildServiceImpl()) {
            TestBuildServiceListener testStoppingBuildServiceListener = new TestBuildServiceListener();
            buildService.buildContinously(testProjectDirectory, "build", testStoppingBuildServiceListener);
            await().atMost(30, SECONDS).until(() -> testStoppingBuildServiceListener.succeeded);
        }
    }

    private static class TestBuildServiceListener implements BuildServiceListener {
        boolean succeeded = false;

        @Override
        public void buildSucceeded() {
            succeeded = true;
        }
    }
}
