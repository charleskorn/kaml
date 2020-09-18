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

plugins {
    `kotlin-dsl`

    apply { id("com.github.ben-manes.versions") version "0.33.0" }
}

repositories {
    maven("https://plugins.gradle.org/m2/")
    jcenter()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

dependencies {
    implementation(group = "com.diffplug.spotless", name = "spotless-plugin-gradle", version = "4.5.1")
    implementation(group = "io.codearte.gradle.nexus", name = "gradle-nexus-staging-plugin", version = "0.21.2")
    implementation(group = "org.ajoberstar.reckon", name = "reckon-gradle", version = "0.12.0")
    implementation(group = "de.marcphilipp.gradle", name = "nexus-publish-plugin", version = "0.4.0")
}
