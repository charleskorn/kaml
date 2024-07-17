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

package com.charleskorn.kaml

import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import java.io.OutputStream

/**
 * Convert [value] to YAML, and output the result into an [OutputStream].
 *
 * The character encoding is UTF-8.
 */
// The character encoding is not configurable, because we use Okio which doesn't support converting
// between UTF-8 and other encodings.
public fun <T> Yaml.encodeToStream(
    serializer: SerializationStrategy<T>,
    value: T,
    stream: OutputStream,
): Unit = encodeToSink(serializer, value, stream.asSink().buffered())

/**
 * Convert [value] to YAML, and output the result into an [OutputStream].
 *
 * The character encoding is UTF-8.
 */
public inline fun <reified T> Yaml.encodeToStream(
    value: T,
    stream: OutputStream,
): Unit = encodeToStream(serializersModule.serializer(), value, stream)
