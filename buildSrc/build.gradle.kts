/*

   Copyright 2018-2019 Charles Korn.

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
}

repositories {
    maven("https://plugins.gradle.org/m2/")
    jcenter()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

dependencies {
    compile(group = "com.diffplug.spotless", name = "spotless-plugin-gradle", version = "3.24.2")
    compile(group = "io.codearte.gradle.nexus", name = "gradle-nexus-staging-plugin", version = "0.12.0")
    compile(group = "org.ajoberstar.reckon", name = "reckon-gradle", version = "0.11.0")
}
