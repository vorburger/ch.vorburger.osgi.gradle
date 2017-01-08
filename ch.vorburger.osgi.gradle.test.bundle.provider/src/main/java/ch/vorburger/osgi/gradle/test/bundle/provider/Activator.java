package ch.vorburger.osgi.gradle.test.bundle.provider;

import ch.vorburger.osgi.gradle.test.bundle.api.TestService;
import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

// TODO remove once @Component on SourceInstallServiceImpl works
public class Activator implements BundleActivator {

    private ServiceRegistration<?> serviceReg;

    @Override
    public void start(BundleContext context) throws Exception {
        serviceReg = context.registerService(TestService.class, new TestServiceImpl(), null);
        Logger.getLogger(getClass().getName()).info("start(): " + serviceReg.getReference());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        serviceReg.unregister();
        Logger.getLogger(getClass().getName()).info("stop()");
    }

}
