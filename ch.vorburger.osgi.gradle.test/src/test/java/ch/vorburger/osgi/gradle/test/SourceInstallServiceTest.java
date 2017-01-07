package ch.vorburger.osgi.gradle.test;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import ch.vorburger.osgi.gradle.SourceInstallService;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;

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
                bundle("file:../org.gradle.tooling.osgi/build/libs/org.gradle.tooling.osgi-3.3.jar"),
                bundle("file:../ch.vorburger.osgi.gradle/build/libs/ch.vorburger.osgi.gradle-1.0.0-SNAPSHOT.jar"),
                junitBundles());
    }

    @Test
    @Ignore // TODO remove @Ignore
    public void testSourceInstallService() throws Exception {
        assertNotNull(sourceInstallService);
        // TODO write TestService.java v1 "welcome, world"
        Future<?> future = sourceInstallService.installSourceBundle(new File("../ch.vorburger.osgi.gradle.test.bundle"));
        // TODO wait for build completion in continous mode instead like this
        try {
            future.get(30, TimeUnit.SECONDS);
            // TODO start bundle
    /*
            ServiceReference<TestService> serviceRef = bundleContext.getServiceReference(TestService.class);
            TestService testService = bundleContext.getService(serviceRef);
            assertEquals("hello, world", testService.sayHello());
    */
            // TODO write TestService.java v2 "hello, world", assert changed

            // TODO stop/close test.bundle, write TestService.java v3, assert no more changes
        } finally {
            future.cancel(true);
        }
    }

}
