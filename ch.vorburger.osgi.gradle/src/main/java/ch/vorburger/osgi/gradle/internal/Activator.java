package ch.vorburger.osgi.gradle.internal;

import ch.vorburger.osgi.gradle.SourceInstallService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

// TODO remove once @Component on SourceInstallServiceImpl works
public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(SourceInstallService.class, new SourceInstallServiceImpl(context), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
