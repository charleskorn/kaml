import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(group = "com.github.mhshams", name = "dahgan", version = "0.5.0")
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = "0.9.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
