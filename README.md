# ch.vorburger.osgi.gradle

HOT reload OSGi bundles from sources, with continuous build through wrapped Gradle Tooling API and Maven Invoker as OSGi bundles!

Also check out the [ch.vorburger.minecraft.osgi project](https://github.com/vorburger/ch.vorburger.minecraft.osgi), for use in which this was originally created. (But this is of course a completely independant project of that, and can be used for anything else similar.)

Always use `./gradlew install` instead of `./gradlew build` (because PAX Exam OSGi Test requires local Maven repo installation).

Licensed under the [Apache License v2.0 (ASL)](LICENSE). Contributions welcome.
