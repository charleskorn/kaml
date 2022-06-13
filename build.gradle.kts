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

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.github.ben-manes.versions")
}

group = "com.charleskorn.kaml"

repositories {
    mavenCentral()
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
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("io.kotest:kotest-assertions-core:5.3.1")
                implementation("io.kotest:kotest-framework-api:5.3.1")
                implementation("io.kotest:kotest-framework-engine:5.3.1")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.snakeyaml:snakeyaml-engine:2.3")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:5.3.1")
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configureAssemble()
configureJacoco()
configurePublishing()
configureSpotless()
configureTesting()
configureVersioning()
configureWrapper()
