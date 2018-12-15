package com.charleskorn.kaml.build

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.repositories
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

fun Project.configureJacoco() {
    apply<JacocoPlugin>()

    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    configure<JacocoPluginExtension> {
        toolVersion = "0.8.3-SNAPSHOT"
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        reports {
            xml.isEnabled = true
        }
    }
}
