package ch.vorburger.osgi.gradle.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
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
        getSingleJarFile().ifPresent(file -> listener.buildSucceeded(file));
    }

    protected Optional<File> getSingleJarFile() {
        File buildLibsDir = new File(projectDirectory, "build/libs");
        File[] jarFiles = buildLibsDir.listFiles((FilenameFilter) (dir, name) -> name.endsWith(".jar"));
        if (jarFiles.length == 0) {
            LOG.error("After succesful build, no *.jar found in: {}", buildLibsDir.getAbsolutePath());
            return Optional.empty();
        } else if (jarFiles.length > 1) {
            LOG.error("After succesful build, more than 1 *.jar/s found: {}", Arrays.toString(jarFiles));
            return Optional.empty();
        } else {
            return Optional.of(jarFiles[0]);
        }
    }

}
