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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;

/**
 * Provider to obtain {@link ExecutorService} implementations.
 *
 * @author Michael Vorburger
 */
public interface ExecutorServiceProvider {

    /**
     * See {@link Executors#newCachedThreadPool()}.
     */
    ListeningExecutorService newCachedThreadPool(Logger logger, String poolName);

    /**
     * See {@link Executors#newFixedThreadPool(int)} and {@link Executors#newSingleThreadExecutor()}.
     */
    ListeningExecutorService newFixedThreadPool(Logger logger, int nThreads, String poolName);

}
