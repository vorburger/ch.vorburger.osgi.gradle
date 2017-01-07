package ch.vorburger.osgi.gradle.internal.concurrent;

public interface ExecutorServiceProviderSingleton {

    ExecutorServiceProvider INSTANCE = new ExecutorServiceProviderImpl();

}
