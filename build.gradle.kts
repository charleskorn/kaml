/*

   Copyright 2018 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.3.11")
    }
}

plugins {
    kotlin("jvm") version "1.3.11"

    apply { id("com.github.ben-manes.versions") version "0.20.0" }
    apply { id("com.diffplug.gradle.spotless") version "3.16.0" }
    apply { id("org.ajoberstar.reckon") version "0.9.0" }
}

apply(plugin = "kotlinx-serialization")

group = "com.charleskorn"

reckon {
    scopeFromProp()
    stageFromProp("dev", "final")
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    compile(kotlin("stdlib-jdk8", "1.3.11"))
    compile(group = "com.github.mhshams", name = "core", version = "0.5.0")
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = "0.9.1")

    val spekVersion = "1.2.1"

    // Override the version of kotlin-reflect used by Spek.
    testImplementation(kotlin("reflect", "1.3.11"))
    testImplementation(group = "org.jetbrains.spek", name = "spek-api", version = spekVersion)
    testImplementation(group = "ch.tutteli.atrium", name = "atrium-cc-en_GB-robstoll", version = "0.7.0")

    testRuntimeOnly(group = "org.jetbrains.spek", name = "spek-junit-platform-engine", version = spekVersion)
    testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-engine", version = "1.3.2")
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

// HACK: I cannot find a way to configure Spotless in a separate file (like we do for wrapper.gradle.kts), so we have to do it here.

val licenseText = """
   Copyright 2018 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
"""

val kotlinLicenseHeader = """/*
$licenseText
*/

"""

spotless {
    format("misc") {
        target(
            fileTree(
                mapOf(
                    "dir" to ".",
                    "include" to listOf("**/*.md", "**/.gitignore", "**/*.yaml", "**/*.yml", "**/*.sh", "**/Dockerfile")
                )
            )
        )

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("0.29.0")

        licenseHeader(kotlinLicenseHeader, "import|tasks")

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }

    kotlin {
        ktlint("0.29.0")

        this.licenseHeader(kotlinLicenseHeader)

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

apply(from = "gradle/jacoco.gradle.kts")
apply(from = "gradle/wrapper.gradle.kts")
