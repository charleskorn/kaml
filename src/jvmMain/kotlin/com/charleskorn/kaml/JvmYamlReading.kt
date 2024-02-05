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
import okio.Buffer
import okio.Source
import okio.Timeout
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset

public fun <T> Yaml.decodeFromStream(deserializer: DeserializationStrategy<T>, source: InputStream, charset: Charset = Charsets.UTF_8): T =
    decodeFromReader(deserializer, InputStreamReader(source, charset))

public inline fun <reified T> Yaml.decodeFromStream(stream: InputStream): T =
    TODO()

public fun Yaml.parseToYamlNode(string: String): YamlNode =
    TODO()

public fun Yaml.parseToYamlNode(source: InputStream): YamlNode =
    TODO()

private fun <T> Yaml.decodeFromReader(deserializer: DeserializationStrategy<T>, source: Reader): T {
    val rootNode = parseToYamlNodeFromReader(source)

    val input = YamlInput.createFor(rootNode, this, serializersModule, configuration, deserializer.descriptor)
    return input.decodeSerializableValue(deserializer)
}

private fun Yaml.parseToYamlNodeFromReader(source: Reader): YamlNode {
    val parser = YamlParser(source.toSource())
    val reader = YamlNodeReader(parser, configuration.extensionDefinitionPrefix, configuration.allowAnchorsAndAliases)
    val node = reader.read()
    parser.ensureEndOfStreamReached()
    return node
}

private fun Reader.toSource(): Source =
    object : Source {
        override fun read(sink: Buffer, byteCount: Long): Long {
            val charArray = CharArray(byteCount.toInt())
            return this@toSource.read(charArray, 0, byteCount.toInt()).toLong()
        }

        override fun timeout(): Timeout {
            // TODO: use some sensible values here
            return Timeout()
        }

        override fun close() {
            this@toSource.close()
        }
    }
