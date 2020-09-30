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

package com.charleskorn.kaml.build

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

val licenseText = """
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
"""

val kotlinLicenseHeader = """/*
$licenseText
*/

"""

fun Project.configureSpotless() {
    apply<SpotlessPlugin>()

    configure<SpotlessExtension>() {
        format("misc") {
            target(
                fileTree(
                    mapOf(
                        "dir" to ".",
                        "include" to listOf("**/*.md", "**/.gitignore", "**/*.yaml", "**/*.yml", "**/*.sh", "**/Dockerfile"),
                        "exclude" to listOf(".gradle/**", ".gradle-cache/**")
                    )
                )
            )

            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }

        kotlinGradle {
            target("*.gradle.kts", "gradle/*.gradle.kts", "buildSrc/*.gradle.kts")
            ktlint("0.36.0")

            @Suppress("INACCESSIBLE_TYPE")
            licenseHeader(kotlinLicenseHeader, "import|tasks|apply|plugins")

            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }

        kotlin {
            target("src/**/*.kt", "buildSrc/**/*.kt")
            ktlint("0.39.0")

            @Suppress("INACCESSIBLE_TYPE")
            licenseHeader(kotlinLicenseHeader)

            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }
    }

    tasks.named("spotlessKotlinCheck") {
        mustRunAfter("test")
    }
}
