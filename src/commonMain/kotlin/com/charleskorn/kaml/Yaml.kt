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

import com.charleskorn.kaml.internal.StringStreamDataWriter
import com.charleskorn.kaml.internal.bufferedSource
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import okio.Sink
import okio.Source
import okio.buffer

public class Yaml(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    public val configuration: YamlConfiguration = YamlConfiguration(),
) : StringFormat {
    public fun <T> decodeFromYamlNode(
        deserializer: DeserializationStrategy<T>,
        node: YamlNode,
    ): T {
        val input = YamlInput.createFor(node, this, serializersModule, configuration, deserializer.descriptor)
        return input.decodeSerializableValue(deserializer)
    }

    public companion object {
        public val default: Yaml = Yaml()
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return decodeFromSource(deserializer, string.bufferedSource())
    }

    public fun <T> decodeFromSource(
        deserializer: DeserializationStrategy<T>,
        source: Source,
    ): T {
        val rootNode = parseToYamlNodeFromSource(source)

        val input = YamlInput.createFor(rootNode, this, serializersModule, configuration, deserializer.descriptor)
        return input.decodeSerializableValue(deserializer)
    }

    public fun parseToYamlNode(string: String): YamlNode = parseToYamlNodeFromSource(string.bufferedSource())

    internal fun parseToYamlNodeFromSource(source: Source): YamlNode {
        val parser = YamlParser(source)
        val reader =
            YamlNodeReader(parser, configuration.extensionDefinitionPrefix, configuration.allowAnchorsAndAliases)
        val node = reader.read()
        parser.ensureEndOfStreamReached()
        return node
    }

    public fun <T> encodeToSink(
        serializer: SerializationStrategy<T>,
        value: T,
        sink: Sink,
    ) {
        val writer = encodeToStreamDataWriter(serializer, value)
        writer.buffer().readAll(sink)
    }

    override fun <T> encodeToString(
        serializer: SerializationStrategy<T>,
        value: T,
    ): String {
        val writer = encodeToStreamDataWriter(serializer, value)
        return writer.buffer().readUtf8().trimEnd()
    }

    private fun <T> encodeToStreamDataWriter(
        serializer: SerializationStrategy<T>,
        value: T,
    ): StringStreamDataWriter {
        val writer = StringStreamDataWriter()
        @OptIn(ExperimentalStdlibApi::class)
        YamlOutput(writer, serializersModule, configuration).use { output ->
            output.encodeSerializableValue(serializer, value)
        }
        return writer
    }
}
