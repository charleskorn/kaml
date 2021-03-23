/*

   Copyright 2018-2021 Charles Korn.

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

import com.charleskorn.kaml.build.configureAssemble
import com.charleskorn.kaml.build.configureJacoco
import com.charleskorn.kaml.build.configurePublishing
import com.charleskorn.kaml.build.configureSpotless
import com.charleskorn.kaml.build.configureTesting
import com.charleskorn.kaml.build.configureVersioning
import com.charleskorn.kaml.build.configureWrapper
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.github.ben-manes.versions")
    id("org.jetbrains.dokka")
}

group = "com.charleskorn.kaml"

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    explicitApi()

    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("org.spekframework.spek2:spek-dsl-metadata:2.0.15")
                implementation("ch.tutteli.atrium:atrium-fluent-en_GB-common:0.15.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.snakeyaml:snakeyaml-engine:2.2.1")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("org.spekframework.spek2:spek-dsl-jvm:2.0.15")
                implementation("ch.tutteli.atrium:atrium-fluent-en_GB:0.15.0")
                runtimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.15")
            }
        }
    }
}

// Dokka doesn't support being configured from another plugin like buildSrc, so we have to configure it here.
// See https://github.com/Kotlin/dokka/issues/1463 for details.
tasks.named<DokkaTask>("dokkaJavadoc") {
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(8)
            skipDeprecated.set(true)
        }
    }
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

configureAssemble()
configureJacoco()
configurePublishing()
configureSpotless()
configureTesting()
configureVersioning()
configureWrapper()
