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
    id("io.kotest.multiplatform") version "5.9.1"
}

group = "com.charleskorn.kaml"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()

    jvm {
        withJava()
    }

    js(IR) {
        browser {
            testTask {
                // TODO: enable once the tests work with Kotlin/JS.
                enabled = false
            }
        }
        nodejs {
            testTask {
                // TODO: enable once the tests work with Kotlin/JS.
                enabled = false
            }
        }
        binaries.executable()
    }

    wasmJs {
        binaries.library()
        browser {
            testTask {
                // TODO: enable once the tests work with Kotlin/Wasm.
                enabled = false
            }
        }
        nodejs {
            testTask {
                // TODO: enable once the tests work with Kotlin/Wasm.
                enabled = false
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.0")
                implementation("it.krzeminski:snakeyaml-engine-kmp:3.0.0")
                implementation("com.squareup.okio:okio:3.9.0")
            }
        }

        commonTest {
            dependencies {
                implementation("io.kotest:kotest-assertions-core:5.9.1")
                implementation("io.kotest:kotest-framework-api:5.9.1")
                implementation("io.kotest:kotest-framework-engine:5.9.1")
                implementation("io.kotest:kotest-framework-datatest:5.9.1")
            }
        }

        jvmTest {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:5.9.1")
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configureAssemble()
configurePublishing()
configureSpotless()
configureTesting()
configureVersioning()
configureWrapper()
