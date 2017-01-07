package ch.vorburger.osgi.gradle.internal;

import ch.vorburger.osgi.gradle.SourceInstallService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

// TODO remove once @Component on SourceInstallServiceImpl works
public class Activator implements BundleActivator {

    private SourceInstallService sourceInstallService;

    @Override
    public void start(BundleContext context) throws Exception {
        sourceInstallService = new SourceInstallServiceImpl(context);
        context.registerService(SourceInstallService.class, sourceInstallService, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        sourceInstallService.close();
    }

}
