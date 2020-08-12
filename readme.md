# Gradle Kotlin DSL Demo

This project shows how to use `kotlin DSL` for `gradle` to generate Java & Kotlin jars.

You can find solutions for:

* copy resources to build path from project's resource dir.
* copy all dependencies to build path for `thin` jar.
* make different file name for `thin` jar and `fat` jar.
* zip all dependencies into `fat` jar.
* use `TestNG` for unit test.

 Only required official plugins.
 
 The code is very simple. There are 3 classes in the project. One Java class, one Kotlin class with main function, and one Kotlin test class using `TestNG`.
 
 `log4j2` is referenced as a third party library.
 The config file is used as resources.

 You can choose `buildThinJar` or `buildFatJar` from `IDEA`\'s Gradle` panel to generate jar file. Core functions are in [build.gradle.kts](build.gradle.kts).
 I think this is the only important thing you should take a look.
 
 **Enjoy!**
 