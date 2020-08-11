import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    kotlin("jvm") version "1.3.72"
}

group = "com.yxy"
version = "1.0.0"

// change fields below for real project.
val implementationVendor = "The YXY company"
val implementationUrl = "http://www.yunxingyu.com"
val implementationTitle = "Demo for gradle kotlin dsl"
val mainClass = "com.yxy.MainKt"

repositories {
    maven("http://maven.aliyun.com/nexus/content/groups/public/")
    maven("https://repo.spring.io/libs-snapshot/")
    maven("http://uk.maven.org/maven2/")
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
    build {
        // redefine build task fun thinJar first.
        dependsOn(thinJar)

        doLast {
            // clean compiled jar file without appendix.
            // thinJar & fatJar set different archive name with appendix.
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
        attributes["Implementation-Title"] = "$implementationTitle, $archiveAppendix"
        attributes["Package-Method"] = ""
        attributes["Implementation-Vendor"] = implementationVendor
        attributes["Implementation-URL"] = implementationUrl
    }

    /* 以下两种方式效果相同。 */
    //    from({
    //        configurations["runtimeClasspath"].map { file ->
    //            if (file.isDirectory) file else zipTree(file)
    //        }
    //    })
    configurations["runtimeClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }

    with(tasks["jar"] as CopySpec)
    exclude("*.*")
}

/**
 * build a fat jar.
 */
val thinJar by tasks.registering(Jar::class) {
    dependsOn(copyDependencies, copyResources)
    archiveAppendix.set("core")
    val cp = configurations["runtimeClasspath"].joinToString(" ") { it.name } + " ."

    with(manifest) {
        attributes["Class-Path"] = cp
        attributes["Main-Class"] = mainClass
        attributes["Implementation-Title"] = "$implementationTitle, $archiveAppendix"
        attributes["Package-Method"] = ""
        attributes["Implementation-Vendor"] = implementationVendor
        attributes["Implementation-URL"] = implementationUrl
    }

    with(tasks["jar"] as CopySpec)
    exclude("*.*")
}
