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
package ch.vorburger.osgi.builder;

import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import org.osgi.framework.Bundle;

/**
 * Install a Bundle from source into OSGi framework (builds it).
 *
 * @author Michael Vorburger
 */
public interface SourceInstallService extends AutoCloseable {

    /**
     * Installs an OSGi bundle from a source code project directory.
     *
     * This method will attempt to automatically find the Bundle JAR output built
     * using some heuristics (looking for *.jar in build/libs/ or target/ but
     * skipping -all.jar; see BuildServiceListenerAdapter).
     *
     * The bundle will NOT be started by this method, yet.
     *
     * @param projectDirectory location of a Gradle or Maven buildable project
     * @return a Future of the installed Bundle
     */
    ListenableFuture<Bundle> installSourceBundle(File projectDirectory);

    /**
     * Installs an OSGi bundle from a source code project directory.
     *
     * The bundle will NOT be started by this method, yet.
     *
     * @param projectDirectory location of a Gradle or Maven buildable project
     * @param relativePathToBuiltBundle path relative to projectDirectory where Bundle JAR was built
     * @return a Future of the installed Bundle
     */
    // TODO ListenableFuture<Bundle> installSourceBundle(File projectDirectory, File relativePathToBuiltBundle);

}
