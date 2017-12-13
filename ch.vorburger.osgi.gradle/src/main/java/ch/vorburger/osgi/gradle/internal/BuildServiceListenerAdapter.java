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
package ch.vorburger.osgi.gradle.internal;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuildServiceListenerAdapter implements BuildServiceListener {

    private static final Logger LOG = LoggerFactory.getLogger(BuildServiceListenerAdapter.class);

    private final File projectDirectory;
    private final BuildServiceSingleFileOutputListener listener;

    public BuildServiceListenerAdapter(File projectDirectory, BuildServiceSingleFileOutputListener listener) {
        this.projectDirectory = projectDirectory;
        this.listener = listener;
    }

    @Override
    public final void buildSucceeded() {
        getSingleJarFile().ifPresent(listener::buildSucceeded);
    }

    protected Optional<File> getSingleJarFile() {
        File buildLibsDir = new File(projectDirectory, "build/libs");
        if (!buildLibsDir.exists()) {
            buildLibsDir = new File(projectDirectory, "target");
        }
        // NB: Filter out any *-all JARs.. Those are typically produced by a "shadow" kinda plugin,
        // and can be used to build "fat" library JARs usable outside of OSGi environments.  For
        // OSGi, we'd instead of BND to embed JARs (differently).
        File[] jarFiles = buildLibsDir.listFiles((dir, name) -> name.endsWith(".jar") && !name.endsWith("-all.jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            LOG.error("After successful build, no *.jar found in: {}", buildLibsDir.getAbsolutePath());
            return Optional.empty();
        } else if (jarFiles.length > 1) {
            List<File> jarFileList = Arrays.asList(jarFiles);
            LOG.error("After successful build, more than 1 *.jar/s found, can't choose, ignoring all: {}", jarFileList);
            return Optional.empty();
        } else {
            return Optional.of(jarFiles[0]);
        }
    }

}
