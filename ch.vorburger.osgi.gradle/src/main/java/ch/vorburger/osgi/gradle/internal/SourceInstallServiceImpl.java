package ch.vorburger.osgi.gradle.internal;

import ch.vorburger.osgi.gradle.SourceInstallService;
import com.google.common.util.concurrent.SettableFuture;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of SourceInstallService.
 *
 * @author Michael Vorburger
 */
// TODO @Component
public class SourceInstallServiceImpl implements SourceInstallService {
    // TODO implements AutoCloseable @Deactivate void close() { buildService.close(); }

    private static final Logger LOG = LoggerFactory.getLogger(SourceInstallServiceImpl.class);

    private final BundleContext bundleContext;
    private final BuildService buildService = new BuildServiceImpl();

    public SourceInstallServiceImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public Future<Bundle> installSourceBundle(File projectDirectory) {
        SettableFuture<Bundle> installFuture = SettableFuture.create();
        Future<?> buildFuture = buildService.buildContinously(projectDirectory, "build", singleProducedFile -> {
            // TODO handle rebuild & update!  NB: Can only set future once..
            try {
                InputStream inputStream = new FileInputStream(singleProducedFile);
                Bundle bundle = bundleContext.installBundle("source:" + projectDirectory.toURI().toString(), inputStream);
                installFuture.set(bundle);
                inputStream.close();
            } catch (BundleException | IOException e) {
                LOG.error("Problem reading/installing bundle JAR built from source: {}", singleProducedFile, e);
                installFuture.setException(e);
            }
        });
        return installFuture;
    }

}