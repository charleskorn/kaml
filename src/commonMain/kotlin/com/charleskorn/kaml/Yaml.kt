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

import com.charleskorn.kaml.internal.bufferedSource
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import okio.Buffer
import okio.BufferedSink
import okio.Sink
import okio.Source
import okio.buffer

public class Yaml(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    public val configuration: YamlConfiguration = YamlConfiguration(),
) : StringFormat {

    public companion object {
        public val default: Yaml = Yaml()
    }

    public fun <T> decodeFromYamlNode(
        deserializer: DeserializationStrategy<T>,
        node: YamlNode,
    ): T {
        val input = YamlInput.createFor(node, this, serializersModule, configuration, deserializer.descriptor)
        return input.decodeSerializableValue(deserializer)
    }

    override fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        string: String,
    ): T {
        return decodeFromSource(deserializer, string.bufferedSource())
    }

    public fun <T> decodeFromSource(
        deserializer: DeserializationStrategy<T>,
        source: Source,
    ): T {
        val rootNode = parseToYamlNode(source)

        val input = YamlInput.createFor(rootNode, this, serializersModule, configuration, deserializer.descriptor)
        return input.decodeSerializableValue(deserializer)
    }

    public fun parseToYamlNode(string: String): YamlNode =
        parseToYamlNode(string.bufferedSource())

    internal fun parseToYamlNode(source: Source): YamlNode {
        val parser = YamlParser(source, configuration.codePointLimit)
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
        encodeToBufferedSink(serializer, value, sink.buffer())
    }

    override fun <T> encodeToString(
        serializer: SerializationStrategy<T>,
        value: T,
    ): String {
        val buffer = Buffer()
        encodeToBufferedSink(serializer, value, buffer)
        return buffer.readUtf8().trimEnd()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun <T> encodeToBufferedSink(
        serializer: SerializationStrategy<T>,
        value: T,
        sink: BufferedSink,
    ) {
        BufferedSinkDataWriter(sink).use { writer ->
            YamlOutput(writer, serializersModule, configuration).use { output ->
                output.encodeSerializableValue(serializer, value)
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private class BufferedSinkDataWriter(
    val sink: BufferedSink,
) : StreamDataWriter, AutoCloseable {
    override fun flush(): Unit = sink.flush()

    override fun write(str: String) {
        sink.writeUtf8(str)
    }

    override fun write(str: String, off: Int, len: Int) {
        sink.writeUtf8(string = str, beginIndex = off, endIndex = off + len)
    }

    override fun close() {
        flush()
    }
}
