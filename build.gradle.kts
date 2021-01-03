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
    id("org.jetbrains.kotlin.plugin.serialization") version "1.4.10"
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

        val jvmMain by getting {
            dependencies {
                implementation(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.snakeyaml:snakeyaml-engine:2.2.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
            }
        }
        val jvmTest by getting {
            val spekVersion = "2.0.15"
            dependencies {
                implementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
                implementation("ch.tutteli.atrium:atrium-fluent-en_GB:0.15.0")
                runtimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
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
