package ch.vorburger.osgi.gradle.internal.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceProviderImpl implements ExecutorServiceProvider {

    @Override
    public ExecutorService newCachedThreadPool() {
        return Executors.unconfigurableExecutorService(Executors.newCachedThreadPool());
    }

}
