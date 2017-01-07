package ch.vorburger.osgi.gradle.internal;

import java.io.File;

@FunctionalInterface
public interface BuildServiceSingleFileOutputListener {

    void buildSucceeded(File singleProducedFile);

}
