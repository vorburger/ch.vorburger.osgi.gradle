package ch.vorburger.osgi.gradle.internal.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceProviderImpl implements ExecutorServiceProvider {

    private final ExecutorService cachedThreadPool = Executors.unconfigurableExecutorService(Executors.newCachedThreadPool());

    @Override
    public ExecutorService getCachedThreadPool() {
        return cachedThreadPool;
    }

}
