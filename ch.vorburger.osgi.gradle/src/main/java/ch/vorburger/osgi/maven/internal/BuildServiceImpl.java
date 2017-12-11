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

package ch.vorburger.osgi.maven.internal;

import ch.vorburger.osgi.gradle.internal.BuildService;
import ch.vorburger.osgi.gradle.internal.BuildServiceListener;
import ch.vorburger.osgi.gradle.internal.concurrent.ExecutorServiceProviderSingleton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.cli.MavenCli;


/**
 *
 */
public class BuildServiceImpl implements BuildService {

    private final ListeningExecutorService executorService;
    private WatchService watcher;


    public BuildServiceImpl() {
        this(ExecutorServiceProviderSingleton.INSTANCE.newCachedThreadPool("BuildService"));
    }

    public BuildServiceImpl(ListeningExecutorService executorService) {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            Logger.getLogger(BuildServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.executorService = executorService;
    }
    
    private ListenableFuture<Void> build(File projectDirectory, String[] tasks, boolean continuous, BuildServiceListener listener) {
        Callable<Void> build = (Callable<Void>) () -> {
            MavenCli cli = new MavenCli();
            cli.doMain(tasks, projectDirectory.getAbsolutePath(), System.out, System.out);
            return null;  
        };
        return executorService.submit((Callable<Void>) () -> {
            if (continuous) {
                Path path = projectDirectory.toPath();
                path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
                while (true) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException x) {
                        return null;
                    }

                    for (WatchEvent<?> event: key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW) {
                            continue;
                        }

                        build.call();
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } else {
                build.call();
            }
            
            return null;
        });
    }
    
    @Override
    public ListenableFuture<Void> build(File projectDirectory, String... tasks) {
        return build(projectDirectory, tasks, false, () -> {});
    }

    @Override
    public ListenableFuture<Void> buildContinously(File projectDirectory, String task, BuildServiceListener listener) {
        return build(projectDirectory, new String[] { task }, true, listener);
    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
