package ch.vorburger.osgi.gradle.internal.tests;

import ch.vorburger.osgi.gradle.internal.BuildService;
import ch.vorburger.osgi.gradle.internal.BuildServiceImpl;
import ch.vorburger.osgi.gradle.internal.BuildServiceListener;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * BuildService Test (non-OSGi).
 *
 * @author Michael Vorburger
 */
public class BuildServiceTest {

    File testProjectDirectory = new File("../ch.vorburger.osgi.gradle.test.bundle");
    BuildService buildService = new BuildServiceImpl();

    @Test
    public void help() throws Exception {
        Future<?> future = buildService.build(testProjectDirectory, "help");
        try {
            future.get(30, TimeUnit.SECONDS);
        } finally {
            future.cancel(true);
        }
    }

    @Test
    public void build() throws Exception {
        Future<?> future = buildService.build(testProjectDirectory, "build");
        try {
            future.get(30, TimeUnit.SECONDS);
        } finally {
            future.cancel(true);
        }
    }

    @Test
    public void buildContinously() throws Exception {
        TestStoppingBuildServiceListener testStoppingBuildServiceListener = new TestStoppingBuildServiceListener();
        final Future<?> future = buildService.buildContinously(testProjectDirectory, "build", testStoppingBuildServiceListener);
        testStoppingBuildServiceListener.future = future;
    }

    private static class TestStoppingBuildServiceListener implements BuildServiceListener {
        Future<?> future;

        @Override
        public void buildSucceeded() {
            future.cancel(true);
        }
    }
}
