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

package com.charleskorn.kaml

import com.charleskorn.kaml.TrimType.TRIM_INDENT
import com.charleskorn.kaml.TrimType.TRIM_MARGIN
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Adds a comment block before property on serialization
 * @property lines comment lines to add
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
public annotation class YamlComment(
    vararg val lines: String,
)

/**
 * Trim type for [YamlComment], if not specified uses [TRIM_INDENT]
 *
 * @see TrimType
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
public annotation class YamlCommentTrim(
    val trimType: TrimType,
)

/**
 * Trim type for [YamlComment.lines]
 *
 * @property TRIM_INDENT uses [String.trimIndent]
 * @property TRIM_MARGIN uses [String.trimMargin]
 */
public enum class TrimType {
    NONE,
    TRIM_INDENT,
    TRIM_MARGIN,
}
