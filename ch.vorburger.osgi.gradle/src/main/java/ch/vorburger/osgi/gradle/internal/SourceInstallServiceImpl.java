package ch.vorburger.osgi.gradle.internal;

import ch.vorburger.osgi.gradle.SourceInstallService;
import java.io.File;
import java.util.concurrent.Future;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Implementation of  SourceInstallService.
 *
 * @author Michael Vorburger
 */
// TODO @Component
public class SourceInstallServiceImpl implements SourceInstallService {

    private final BundleContext bundleContext;
    private final BuildService buildService = new BuildServiceImpl();

    public SourceInstallServiceImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public Future<Bundle> installSourceBundle(File projectDirectory) {
        Future<?> buildFuture = buildService.buildContinously(projectDirectory, "build", /* TODO */ null);
        // InputStream inputStream;
        // TODO bundleContext.installBundle("source:" + projectDirectory.toURI().toString(), inputStream);
        return null; // TODO !
    }

}
