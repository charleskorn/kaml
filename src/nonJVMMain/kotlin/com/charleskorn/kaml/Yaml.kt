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
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.Source
import org.snakeyaml.engine.v2.api.StreamDataWriter

public actual class Yaml(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    public actual val configuration: YamlConfiguration = YamlConfiguration(),
) : StringFormat {
    public actual fun <T> decodeFromYamlNode(
        deserializer: DeserializationStrategy<T>,
        node: YamlNode,
    ): T {
        val input = YamlInput.createFor(node, this, serializersModule, configuration, deserializer.descriptor)
        return input.decodeSerializableValue(deserializer)
    }

    public actual companion object {
        public actual val default: Yaml = Yaml() }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return decodeFromReader(deserializer, Buffer().write(string.encodeUtf8()))
    }

    private fun <T> decodeFromReader(deserializer: DeserializationStrategy<T>, source: Source): T {
        val rootNode = parseToYamlNodeFromReader(source)

        val input = YamlInput.createFor(rootNode, this, serializersModule, configuration, deserializer.descriptor)
        return input.decodeSerializableValue(deserializer)
    }

    private fun parseToYamlNodeFromReader(source: Source): YamlNode {
        val parser = YamlParser(source)
        val reader = YamlNodeReader(parser, configuration.extensionDefinitionPrefix, configuration.allowAnchorsAndAliases)
        val node = reader.read()
        parser.ensureEndOfStreamReached()
        return node
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        val writer = object : StreamDataWriter {
            private var stringBuilder = StringBuilder()

            override fun flush() { }

            override fun write(str: String) {
                stringBuilder.append(str)
            }

            override fun write(str: String, off: Int, len: Int) {
                stringBuilder.append(str.drop(off).subSequence(0, len))
            }

            override fun toString(): String {
                return stringBuilder.toString()
            }
        }

        encodeToStreamDataWriter(serializer, value, writer)

        return writer.toString().trimEnd()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun <T> encodeToStreamDataWriter(serializer: SerializationStrategy<T>, value: T, writer: StreamDataWriter) {
        YamlOutput(writer, serializersModule, configuration).use { output ->
            output.encodeSerializableValue(serializer, value)
        }
    }
}
