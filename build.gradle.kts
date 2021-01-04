/*

   Copyright 2018-2020 Charles Korn.

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

import com.charleskorn.kaml.build.Versions
import com.charleskorn.kaml.build.configureAssemble
import com.charleskorn.kaml.build.configureJacoco
import com.charleskorn.kaml.build.configurePublishing
import com.charleskorn.kaml.build.configureSpotless
import com.charleskorn.kaml.build.configureTesting
import com.charleskorn.kaml.build.configureVersioning
import com.charleskorn.kaml.build.configureWrapper

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    kotlin("multiplatform") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.ben-manes.versions") version "0.36.0"
}

group = "com.charleskorn.kaml"

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serialization}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.spekframework.spek2:spek-dsl-metadata:${Versions.spek}")
                implementation("ch.tutteli.atrium:atrium-fluent-en_GB-common:${Versions.atrium}")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.snakeyaml:snakeyaml-engine:${Versions.snakeYaml}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.spekframework.spek2:spek-dsl-jvm:${Versions.spek}")
                implementation("ch.tutteli.atrium:atrium-fluent-en_GB:${Versions.atrium}")
                runtimeOnly("org.spekframework.spek2:spek-runner-junit5:${Versions.spek}")
            }
        }
    }
}

configureAssemble()
configureJacoco()
configurePublishing()
configureSpotless()
configureTesting()
configureVersioning()
configureWrapper()
