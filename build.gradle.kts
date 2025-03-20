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
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("io.kotest.multiplatform") version "6.0.0.M2"
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
        browser()
        nodejs()
        binaries.executable()
    }

    wasmJs {
        binaries.library()
        browser()
        nodejs()
    }

    // According to https://kotlinlang.org/docs/native-target-support.html
    // Tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // Tier 2
    linuxX64()
    linuxArm64()
    iosArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()

    // Tier 3
    mingwX64()

    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")
                implementation("it.krzeminski:snakeyaml-engine-kmp:3.1.1")
                implementation("com.squareup.okio:okio:3.10.2")
            }
        }

        commonTest {
            dependencies {
                implementation("io.kotest:kotest-assertions-core:6.0.0.M1")
                implementation("io.kotest:kotest-framework-api:6.0.0.M1")
                implementation("io.kotest:kotest-framework-engine:6.0.0.M1")
                // Overriding coroutines' version to solve a problem with WASM JS tests.
                // See https://kotlinlang.slack.com/archives/CDFP59223/p1736191408326039?thread_ts=1734964013.996149&cid=CDFP59223
                runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }

        jvmTest {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:6.0.0.M1")
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

tasks.withType<KotlinJsIrLink>().configureEach {
    compilerOptions {
        // Catching IndexOutOfBoundsException in Kotlin/Wasm is impossible by default,
        // unless we enable "-Xwasm-enable-array-range-checks" compiler flag.
        // We rely on it in the tests, see https://github.com/charleskorn/kaml/blob/108b48fb560559f0d0724559bb8c7fff631503f9/src/commonTest/kotlin/com/charleskorn/kaml/YamlListTest.kt#L79
        // See https://youtrack.jetbrains.com/issue/KT-59081/
        freeCompilerArgs.add("-Xwasm-enable-array-range-checks")
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
