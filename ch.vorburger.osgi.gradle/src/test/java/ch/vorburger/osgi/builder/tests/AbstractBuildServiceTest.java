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
package ch.vorburger.osgi.builder.tests;

import java.io.File;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import ch.vorburger.osgi.builder.internal.BuildService;
import ch.vorburger.osgi.builder.internal.BuildServiceListener;

/**
 * Base class for the builder services
 */
abstract class AbstractBuildServiceTest {

    abstract BuildService createBuilderUnderTest();

    void testBuild(File testProjectDirectory, String task) throws Exception {
        try (BuildService buildService = createBuilderUnderTest()) {
            Future<?> future = buildService.build(testProjectDirectory, task);
            try {
                future.get(30, SECONDS);
            } finally {
                future.cancel(true);
            }
        }
    }

    void testBuildContinuously(File testProjectDirectory, String task) throws Exception {
        try (BuildService buildService = createBuilderUnderTest()) {
            TestBuildServiceListener testStoppingBuildServiceListener = new TestBuildServiceListener();
            buildService.buildContinously(testProjectDirectory, task, testStoppingBuildServiceListener);
            await().atMost(30, SECONDS).until(() -> testStoppingBuildServiceListener.succeeded);
        }
    }

    protected static class TestBuildServiceListener implements BuildServiceListener {
        boolean succeeded = false;

        @Override
        public void buildSucceeded() {
            succeeded = true;
        }
    }
}
