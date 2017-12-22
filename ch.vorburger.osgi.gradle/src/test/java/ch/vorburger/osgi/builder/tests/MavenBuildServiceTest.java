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
import ch.vorburger.osgi.builder.internal.BuildService;
import ch.vorburger.osgi.builder.maven.internal.MavenBuildService;
import org.junit.Test;

/**
 * {@link MavenBuildService} Test (non-OSGi).
 */
public class MavenBuildServiceTest extends AbstractBuildServiceTest {

    private static final File PROJECT_DIRECTORY = new File("src/test/test-maven-project");

    @Test
    public void build() throws Exception {
        testBuild(PROJECT_DIRECTORY, "package");
    }

    @Test
    public void buildContinuously() throws Exception {
        testBuildContinuously(PROJECT_DIRECTORY, "package");
    }

    @Override
    BuildService createBuilderUnderTest() {
        return new MavenBuildService();
    }
}
