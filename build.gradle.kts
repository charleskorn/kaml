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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin("serialization", version = "1.4.10"))
    }
}

plugins {
    kotlin("jvm") version "1.4.10"

    apply { id("com.github.ben-manes.versions") version "0.33.0" }
}

apply(plugin = "kotlinx-serialization")

group = "com.charleskorn.kaml"

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation(kotlin("stdlib-jdk8"))
    implementation(group = "org.snakeyaml", name = "snakeyaml-engine", version = "2.1")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core", version = "1.0.0")

    val spekVersion = "2.0.13"

    testImplementation(group = "org.spekframework.spek2", name = "spek-dsl-jvm", version = spekVersion)
    testImplementation(group = "ch.tutteli.atrium", name = "atrium-fluent-en_GB", version = "0.13.0")

    testRuntimeOnly(group = "org.spekframework.spek2", name = "spek-runner-junit5", version = spekVersion)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.RequiresOptIn")
}

kotlin {
    explicitApi()
}

configureAssemble()
configureJacoco()
configurePublishing()
configureSpotless()
configureTesting()
configureVersioning()
configureWrapper()
