package ch.vorburger.osgi.gradle.test.bundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

// TODO remove once @Component on SourceInstallServiceImpl works
public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(TestService.class, new TestService(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
