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
package ch.vorburger.osgi.gradle.internal.concurrent;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;

public class ExecutorServiceProviderImpl implements ExecutorServiceProvider {

    @Override
    public ListeningExecutorService newCachedThreadPool(Logger logger, String poolName) {
        return MoreExecutors.listeningDecorator(
                Executors.unconfigurableExecutorService(
                        Executors.newCachedThreadPool(newNamedThreadFactory(logger, poolName))));
    }

    @Override
    public ListeningExecutorService newFixedThreadPool(Logger logger, int nThreads, String poolName) {
        if (nThreads != 1) {
            return MoreExecutors.listeningDecorator(
                    Executors.unconfigurableExecutorService(
                            Executors.newFixedThreadPool(nThreads, newNamedThreadFactory(logger, poolName))));
        } else {
            return MoreExecutors.listeningDecorator(
                    Executors.unconfigurableExecutorService(
                            Executors.newSingleThreadExecutor(newNamedThreadFactory(logger, poolName))));
        }
    }

    private ThreadFactory newNamedThreadFactory(Logger logger, String poolName) {
        // as in https://github.com/opendaylight/infrautils/blob/master/common/util/src/main/java/org/opendaylight/infrautils/utils/concurrent/ThreadFactoryProvider.java
        return new ThreadFactoryBuilder()
                .setNameFormat(poolName + "-%d")
                .setUncaughtExceptionHandler((thread, throwable) -> logger.error("Thread terminated due to uncaught exception: {}", thread.getName(), throwable))
                .build();
    }

}
