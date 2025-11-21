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

package com.charleskorn.kaml.helpers

import com.charleskorn.kaml.YamlNodeScalarStyle
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar

data class ResultWithStyle(
    val content: String,
    val style: YamlNodeScalarStyle,
) {
    override fun toString(): String {
        return content
    }

    fun toYamlScalar(path: YamlPath): YamlScalar =
        YamlScalar(this.content, path, this.style)
}
fun String.plain() = ResultWithStyle(this, YamlNodeScalarStyle.PLAIN)
fun String.doubleQuoted() = ResultWithStyle(this, YamlNodeScalarStyle.DOUBLE_QUOTED)
fun String.singleQuoted() = ResultWithStyle(this, YamlNodeScalarStyle.SINGLE_QUOTED)
fun String.folded() = ResultWithStyle(this, YamlNodeScalarStyle.FOLDED)
fun String.literal() = ResultWithStyle(this, YamlNodeScalarStyle.LITERAL)

fun plainScalar(content: String, yamlPath: YamlPath) = YamlScalar(content, yamlPath, YamlNodeScalarStyle.PLAIN)
