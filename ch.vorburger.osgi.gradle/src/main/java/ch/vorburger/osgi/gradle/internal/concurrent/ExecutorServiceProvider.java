package ch.vorburger.osgi.gradle.internal.concurrent;

import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provider to obtain {@link ExecutorService} implementations.
 *
 * @author Michael Vorburger
 */
public interface ExecutorServiceProvider {

    /**
     * See {@link Executors#newCachedThreadPool()}.
     */
    ListeningExecutorService newCachedThreadPool(String poolName);

    /**
     * See {@link Executors#newWorkStealingPool()}.
     */
    ListeningExecutorService newWorkStealingPool(String poolName);

    /**
     * See {@link Executors#newFixedThreadPool(int)} and {@link Executors#newSingleThreadExecutor()}.
     */
    ListeningExecutorService newFixedThreadPool(int nThreads, String poolName);

    // TODO add ScheduledExecutorService variants...

}
