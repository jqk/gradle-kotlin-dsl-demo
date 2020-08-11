import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    kotlin("jvm") version "1.3.72"
}

group = "notadream"
version = "1.0.0"

// change fields below for real project.
val implementationVendor = "The Not a Dream Co.,Ltd"
val implementationUrl = "http://www.notadream.com"
val implementationTitle = "Demo for Gradle Kotlin DSL"
val mainClass = "notadream.MainKt"

repositories {
    maven("https://maven.aliyun.com/nexus/content/groups/public/")
    maven("https://repo.spring.io/libs-snapshot/")
    maven("https://uk.maven.org/maven2/")
    mavenCentral()
    jcenter()
}

// for dependencies version if use multiple times.
val log4j2Version = "2.13.3"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.logging.log4j", "log4j-api", log4j2Version)

    implementation(fileTree(mapOf("dir" to "../libs", "include" to listOf("*.jar"))))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    compileOnly("org.jetbrains", "annotations", "20.0.0")

    runtimeOnly("org.apache.logging.log4j", "log4j-core", log4j2Version)

    testImplementation("org.testng", "testng", "7.3.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

// for tasks.
val jarPath = "${relativePath(buildDir)}/libs"

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileJava {
        options.encoding = "UTF-8"
    }
    compileTestJava {
        options.encoding = "UTF-8"
    }
    javadoc {
        options.encoding = "UTF-8"
    }
    // customized build task is not required. Remove it if you don't like it.
    build {
        println("build is called.................")

        // redefine build task: thinJar first.
        dependsOn(thinJar)

        doLast {
            println("removing archiveFile............")

            // thinJar & fatJar set different archive name with appendix.
            // So delete compiled jar file without appendix. You can run 'jar' to see what happened.
            val f = jar.get().archiveFile.get().asFile
            if (f.exists()) {
                f.delete()
            }
        }
    }
    test {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events.addAll(listOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED))
        }

        setForkEvery(2)
        val n = Runtime.getRuntime().availableProcessors()
        maxParallelForks = if (n > 1) n / 2 else 1

        // not required for JUnit.
        useTestNG()
    }
}

/**
 * copy all runtime dependencies to build path for thin jar.
 */
val copyDependencies by tasks.registering(Copy::class) {
    /*
    * same effect for sentence below, but the second one is more coding friendly.
    * In fact, configurations.runtimeClasspath.get() is configurations["runtimeClasspath"].
    */
    // from(configurations["runtimeClasspath"])
    from(configurations.runtimeClasspath)
    into(jarPath)
}

/**
 * copy resources to build path for thin jar and fat jar.
 */
val copyResources by tasks.registering(Copy::class) {
    from("$projectDir/src/main/resources")
    into(jarPath)
}

/**
 * build a fat jar.
 */
val fatJar by tasks.registering(Jar::class) {
    dependsOn(copyResources)
    archiveAppendix.set("all")

    with(manifest) {
        attributes["Class-Path"] = ". "
        attributes["Main-Class"] = mainClass
        attributes["Implementation-Title"] = "$implementationTitle, ${archiveAppendix.get()}"
        attributes["Package-Method"] = ""
        attributes["Implementation-Vendor"] = implementationVendor
        attributes["Implementation-URL"] = implementationUrl
    }

    /* Same effect for these tow: */
    //    from({
    //        configurations["runtimeClasspath"].map { file ->
    //            if (file.isDirectory) file else zipTree(file)
    //        }
    //    })
    configurations["runtimeClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }

    with(tasks["jar"] as CopySpec)
}

/**
 * build a thin jar.
 */
val thinJar by tasks.registering(Jar::class) {
    println("thinJar is called...............")

    dependsOn(copyDependencies, copyResources)

    archiveAppendix.set("core")
    val cp = configurations["runtimeClasspath"].joinToString(" ") { it.name } + " ."

    with(manifest) {
        attributes["Class-Path"] = cp
        attributes["Main-Class"] = mainClass
        attributes["Implementation-Title"] = "$implementationTitle, ${archiveAppendix.get()}"
        attributes["Package-Method"] = ""
        attributes["Implementation-Vendor"] = implementationVendor
        attributes["Implementation-URL"] = implementationUrl
    }

    with(tasks.jar.get() as CopySpec)
}
