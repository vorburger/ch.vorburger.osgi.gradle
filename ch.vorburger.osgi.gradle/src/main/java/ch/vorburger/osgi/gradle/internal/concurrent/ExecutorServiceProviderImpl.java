package ch.vorburger.osgi.gradle.internal.concurrent;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ExecutorServiceProviderImpl implements ExecutorServiceProvider {

    @Override
    public ListeningExecutorService newCachedThreadPool(String poolName) {
        return MoreExecutors.listeningDecorator(
                Executors.unconfigurableExecutorService(
                        Executors.newCachedThreadPool(newNamedThreadFactory(poolName))));
    }

    private ThreadFactory newNamedThreadFactory(String poolName) {
        // TODO implement creating pools with Thread name set to poolName-#
        return Executors.defaultThreadFactory();
    }

}
