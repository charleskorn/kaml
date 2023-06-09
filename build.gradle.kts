/*

   Copyright 2018-2023 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

group = "com.charleskorn.kaml"

repositories {
    mavenCentral()

    maven("https://raw.githubusercontent.com/krzema12/snakeyaml-engine-kmp/artifacts/m2/") {
        name = "SnakeYAML Engine KMP Snapshots"
        mavenContent {
            // only include the relevant snapshots
            includeGroup("org.snakeyaml")
            snapshotsOnly()
        }
    }
}

kotlin {
    explicitApi()

    jvm {
        withJava()
    }

    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
            }
        }

        commonTest {
            dependencies {
                implementation("io.kotest:kotest-assertions-core:5.6.2")
                implementation("io.kotest:kotest-framework-api:5.6.2")
                implementation("io.kotest:kotest-framework-engine:5.6.2")
            }
        }

        named("jvmMain") {
            dependencies {
                implementation("org.snakeyaml:snakeyaml-engine:2.6")
            }
        }

        named("jvmTest") {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:5.6.2")
            }
        }

        named("jsTest") {
            dependencies {
                implementation("org.snakeyaml:snakeyaml-engine-kmp:2.7-SNAPSHOT")
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
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
