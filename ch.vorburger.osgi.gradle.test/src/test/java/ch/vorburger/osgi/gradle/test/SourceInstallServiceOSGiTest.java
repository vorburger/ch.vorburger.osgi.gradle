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
package ch.vorburger.osgi.gradle.test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import ch.vorburger.osgi.gradle.SourceInstallService;
import ch.vorburger.osgi.gradle.test.bundle.api.TestService;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.After;
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
public class SourceInstallServiceOSGiTest implements AutoCloseable {

    final File testBundleProjectDir = new File("../ch.vorburger.osgi.gradle.test.bundle.provider");
    final File testBundleSourceFile = new File(testBundleProjectDir, "src/main/java/ch/vorburger/osgi/gradle/test/bundle/provider/TestServiceImpl.java");

    @Inject BundleContext bundleContext;
    @Inject SourceInstallService sourceInstallService;

/*
    @Before
    public void before() {
        bundleContext.addBundleListener(new LoggingBundleListener());
        bundleContext.addServiceListener(new LoggingServiceListener());
    }
*/
    @After
    @Override
    public void close() throws Exception {
        sourceInstallService.close();
    }

    @Configuration
    public Option[] config() {
        return options(
                systemProperty("pax.exam.osgi.unresolved.fail").value("true"),
                mavenBundle("com.google.guava", "guava", "17.0"),
                wrappedBundle(maven("org.awaitility", "awaitility", "2.0.0")),
                bundle("file:../org.gradle.tooling.osgi/build/libs/org.gradle.tooling.osgi-4.5.jar"),
                // DO NOT use wrappedBundle(maven("org.apache.maven.shared", "maven-invoker", "3.0.0")),
                // because it makes deployment easier if the ch.vorburger.osgi.gradle bundle just
                // embeds the maven-invoker and thus works without requiring wrap.
                bundle("file:../ch.vorburger.osgi.gradle/build/libs/ch.vorburger.osgi.gradle-1.0.0-SNAPSHOT.jar"),
                bundle("file:../ch.vorburger.osgi.gradle.test.bundle.api/build/libs/ch.vorburger.osgi.gradle.test.bundle.api-1.0.0-SNAPSHOT.jar"),
                junitBundles());
    }

    @Test
    public void testSourceInstallService() throws Exception {
        assertNotNull(sourceInstallService);
        changeTestService("howdy, world");
        Future<Bundle> futureBundle = sourceInstallService.installSourceBundle(testBundleProjectDir);
        try {
            Bundle bundle = futureBundle.get(30, TimeUnit.SECONDS);
            bundle.start();
            assertThat(sayHello(), is("howdy, world"));

            changeTestService("changed, world");
            await().atMost(10, SECONDS).until(() -> sayHello(), is("changed, world"));
        } finally {
            changeTestService("hello, world");
            futureBundle.cancel(true);
        }
    }

    private void changeTestService(String helloMessage) throws IOException {
        String javaSource = Files.toString(testBundleSourceFile, Charsets.UTF_8);
        javaSource = javaSource.replaceFirst("/\\* ### \\*/ \".*\"", "/* ### */ \"" + helloMessage + "\"");
        Files.write(javaSource, testBundleSourceFile, Charsets.UTF_8);
    }

    private String sayHello() {
        ServiceReference<TestService> serviceRef = bundleContext.getServiceReference(TestService.class);
        if (serviceRef != null) {
            TestService testService = bundleContext.getService(serviceRef);
            return testService.sayHello();
        } else {
            return null;
        }
    }

}
