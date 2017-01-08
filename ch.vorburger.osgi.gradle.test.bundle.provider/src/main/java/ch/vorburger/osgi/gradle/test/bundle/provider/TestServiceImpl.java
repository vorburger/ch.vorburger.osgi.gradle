package ch.vorburger.osgi.gradle.test.bundle.provider;

import ch.vorburger.osgi.gradle.test.bundle.api.TestService;

public class TestServiceImpl implements TestService {

    @Override
    public String sayHello() {
        return /* ### */ "hello, world";
    }

}
