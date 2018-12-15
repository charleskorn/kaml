package com.charleskorn.kaml.build

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.withType

fun Project.configureTesting() {
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
}
