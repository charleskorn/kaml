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
import okio.Source
import okio.source
import java.io.InputStream
import java.nio.charset.Charset

public fun <T> Yaml.decodeFromStream(
    deserializer: DeserializationStrategy<T>,
    source: InputStream,
    charset: Charset = Charsets.UTF_8,
): T {
    return decodeFromSource(
        deserializer,
        source.source(),
        charset,
    )
}

public inline fun <reified T> Yaml.decodeFromStream(
    stream: InputStream
): T =
    decodeFromSource(
        deserializer = serializersModule.serializer<T>(),
        source = stream.source(),
    )

public fun Yaml.parseToYamlNode(
    source: InputStream
): YamlNode =
    parseToYamlNodeFromSource(source.source())

public fun <T> Yaml.decodeFromSource(
    deserializer: DeserializationStrategy<T>,
    source: Source,
    charset: Charset = Charsets.UTF_8, // TODO convert charsets to UTF8 https://kotlinlang.slack.com/archives/C5HT9AL7Q/p1685660615754469?thread_ts=1685549089.185459&cid=C5HT9AL7Q
): T =
    decodeFromSource(
        deserializer,
        source,
    )
