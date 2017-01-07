package ch.vorburger.osgi.gradle.internal.tests;

import ch.vorburger.osgi.gradle.internal.BuildService;
import ch.vorburger.osgi.gradle.internal.BuildServiceImpl;
import ch.vorburger.osgi.gradle.internal.BuildServiceListener;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.Test;

/**
 * BuildService Test (non-OSGi).
 *
 * @author Michael Vorburger
 */
public class BuildServiceTest {

    File testProjectDirectory = new File("../ch.vorburger.osgi.gradle.test.bundle");

    @Test
    public void help() throws Exception {
        try (BuildService buildService = new BuildServiceImpl()) {
            Future<?> future = buildService.build(testProjectDirectory, "help");
            try {
                future.get(30, TimeUnit.SECONDS);
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
                future.get(30, TimeUnit.SECONDS);
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
            Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> testStoppingBuildServiceListener.succeeded);
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
