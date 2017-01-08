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
