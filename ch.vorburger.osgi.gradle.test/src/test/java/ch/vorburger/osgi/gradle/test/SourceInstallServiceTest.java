package ch.vorburger.osgi.gradle.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import ch.vorburger.osgi.gradle.SourceInstallService;
import ch.vorburger.osgi.gradle.test.bundle.TestService;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * SourceInstallService OSGi Integration Test.
 *
 * @author Michael Vorburger
 */
@RunWith(PaxExam.class)
public class SourceInstallServiceTest {

    @Inject BundleContext bundleContext;
    @Inject SourceInstallService sourceInstallService;

    @Configuration
    public Option[] config() {
        return options(
                systemProperty("pax.exam.osgi.unresolved.fail").value("true"),
                mavenBundle("com.google.guava", "guava", "20.0"),
                bundle("file:../org.gradle.tooling.osgi/build/libs/org.gradle.tooling.osgi-3.3.jar"),
                bundle("file:../ch.vorburger.osgi.gradle/build/libs/ch.vorburger.osgi.gradle-1.0.0-SNAPSHOT.jar"),
                junitBundles());
    }

    @Test
    public void testSourceInstallService() throws Exception {
        assertNotNull(sourceInstallService);
        // TODO write TestService.java v1 "welcome, world"
        Future<Bundle> futureBundle = sourceInstallService.installSourceBundle(new File("../ch.vorburger.osgi.gradle.test.bundle"));
        try {
            Bundle bundle = futureBundle.get(30, TimeUnit.SECONDS);
            bundle.start();
            ServiceReference<TestService> serviceRef = bundleContext.getServiceReference(TestService.class);
            TestService testService = bundleContext.getService(serviceRef);
            assertEquals("hello, world", testService.sayHello());

            // TODO write TestService.java v2 "hello, world", assert changed

            // TODO stop/close test.bundle, write TestService.java v3, assert no more changes
        } finally {
            futureBundle.cancel(true);
        }
    }

}
