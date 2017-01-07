package ch.vorburger.osgi.gradle.internal.concurrent;

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
    ExecutorService newCachedThreadPool(String poolName);

}
