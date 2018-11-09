import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.3.0")
    }
}

plugins {
    kotlin("jvm") version "1.3.0"
}

apply(plugin = "kotlinx-serialization")

group = "com.charleskorn"

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    // Override the version of kotlin-reflect used by Spek.
    // We don't have to specify a version ourselves (the Kotlin plugin will do that for us),
    // but if we don't reference it here, the version that Spek refers to is used.
    compile(kotlin("reflect"))
    compile(group = "com.github.mhshams", name = "core", version = "0.5.0")
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = "0.9.0")

    val spekVersion = "1.2.1"

    testImplementation(group = "org.jetbrains.spek", name = "spek-api", version = spekVersion)
    testImplementation(group = "ch.tutteli.atrium", name = "atrium-cc-en_GB-robstoll", version = "0.7.0")

    testRuntimeOnly(group = "org.jetbrains.spek", name = "spek-junit-platform-engine", version = spekVersion)
    testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-engine", version = "1.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("spek")
    }

    testLogging {
        events("failed")
        events("skipped")
        events("standard_out")
        events("standard_error")

        showExceptions = true
        showStackTraces = true
        showCauses = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}
