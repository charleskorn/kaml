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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.serializer
import okio.source
import java.io.InputStream

/**
 * Decode a YAML value [T] from an [InputStream].
 *
 * [InputStream] must be encoded with UTF-8.
 */
// The character encoding is not configurable, because we use Okio which doesn't support converting
// between UTF-8 and other encodings.
public fun <T> Yaml.decodeFromStream(
    deserializer: DeserializationStrategy<T>,
    source: InputStream,
): T = decodeFromSource(
    deserializer = deserializer,
    source = source.source(),
)

/**
 * Decode a YAML value [T] from an [InputStream].
 *
 * [InputStream] must be encoded with UTF-8.
 */
public inline fun <reified T> Yaml.decodeFromStream(
    stream: InputStream,
): T = decodeFromSource(
    deserializer = serializersModule.serializer<T>(),
    source = stream.source(),
)

/**
 *
 * Decode a [YamlNode] from an [InputStream].
 *
 * [InputStream] must be encoded with UTF-8.
 */
public fun Yaml.parseToYamlNode(
    source: InputStream,
): YamlNode =
    parseToYamlNode(source.source())
