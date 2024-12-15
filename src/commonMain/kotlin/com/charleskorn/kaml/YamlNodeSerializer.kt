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

@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)

package com.charleskorn.kaml

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure

internal object YamlNodeSerializer : KSerializer<YamlNode> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("com.charleskorn.kaml.YamlNode", PolymorphicKind.SEALED) {
            annotations += YamlContentPolymorphicSerializer.Marker()
        }.nullable

    override fun serialize(encoder: Encoder, value: YamlNode) {
        encoder.asYamlOutput()
        when (value) {
            is YamlList -> encoder.encodeSerializableValue(YamlListSerializer, value)
            is YamlMap -> encoder.encodeSerializableValue(YamlMapSerializer, value)
            is YamlNull -> encoder.encodeSerializableValue(YamlNullSerializer, value)
            is YamlScalar -> encoder.encodeSerializableValue(YamlScalarSerializer, value)
            is YamlTaggedNode -> encoder.encodeSerializableValue(YamlTaggedNodeSerializer, value)
        }
    }

    override fun deserialize(decoder: Decoder): YamlNode {
        val input = decoder.asYamlInput<YamlInput>()
        return if (input is YamlPolymorphicInput) YamlTaggedNode(input.typeName, input.node) else input.node
    }
}

internal object YamlScalarSerializer : KSerializer<YamlScalar> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.charleskorn.kaml.YamlScalar", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: YamlScalar) {
        encoder.asYamlOutput()
        value.toBooleanOrNull()?.also { return encoder.encodeBoolean(it) }
        value.toLongOrNull()?.also { return encoder.encodeLong(it) }
        value.toDoubleOrNull()?.also { return encoder.encodeDouble(it) }
        value.toCharOrNull()?.also { return encoder.encodeChar(it) }
        encoder.encodeString(value.content)
    }

    override fun deserialize(decoder: Decoder): YamlScalar {
        val result = decoder.asYamlInput<YamlScalarInput>()
        return result.scalar
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal object YamlNullSerializer : KSerializer<YamlNull> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("com.charleskorn.kaml.YamlNull", SerialKind.ENUM)

    override fun serialize(encoder: Encoder, value: YamlNull) {
        encoder.asYamlOutput().encodeNull()
    }

    override fun deserialize(decoder: Decoder): YamlNull {
        val input = decoder.asYamlInput<YamlNullInput>()
        return input.nullValue
    }
}

internal object YamlTaggedNodeSerializer : KSerializer<YamlTaggedNode> {

    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("com.charleskorn.kaml.YamlTaggedNode", PolymorphicKind.OPEN) {
            element("tag", String.serializer().descriptor)
            element("node", YamlNodeSerializer.descriptor)
        }

    override fun serialize(encoder: Encoder, value: YamlTaggedNode) {
        encoder.asYamlOutput().encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.tag)
            encodeSerializableElement(descriptor, 1, YamlNodeSerializer, value.innerNode)
        }
    }

    override fun deserialize(decoder: Decoder): YamlTaggedNode {
        val input = decoder.asYamlInput<YamlPolymorphicInput>()
        return YamlTaggedNode(input.typeName, input.contentNode)
    }
}

internal object YamlMapSerializer : KSerializer<YamlMap> {

    private object YamlMapDescriptor :
        SerialDescriptor by MapSerializer(YamlScalarSerializer, YamlNodeSerializer).descriptor {
        override val serialName: String = "com.charleskorn.kaml.YamlMap"
    }

    override val descriptor: SerialDescriptor = YamlMapDescriptor

    override fun serialize(encoder: Encoder, value: YamlMap) {
        encoder.asYamlOutput()
        MapSerializer(YamlScalarSerializer, YamlNodeSerializer).serialize(encoder, value.entries)
    }

    override fun deserialize(decoder: Decoder): YamlMap {
        val input = decoder.asYamlInput<YamlMapInput>()
        return input.node as YamlMap
    }
}

internal object YamlListSerializer : KSerializer<YamlList> {

    private object YamlListDescriptor : SerialDescriptor by ListSerializer(YamlNodeSerializer).descriptor {
        override val serialName: String = "com.charleskorn.kaml.YamlList"
    }

    override val descriptor: SerialDescriptor = YamlListDescriptor

    override fun serialize(encoder: Encoder, value: YamlList) {
        encoder.asYamlOutput()
        ListSerializer(YamlNodeSerializer).serialize(encoder, value.items)
    }

    override fun deserialize(decoder: Decoder): YamlList {
        val input = decoder.asYamlInput<YamlListInput>()
        return input.list
    }
}

private inline fun <reified I : YamlInput> Decoder.asYamlInput(): I = checkNotNull(this as? I) {
    "This serializer can be used only with Yaml format. Expected Decoder to be ${I::class.simpleName}, got ${this::class}"
}

private fun Encoder.asYamlOutput() = checkNotNull(this as? YamlOutput) {
    "This serializer can be used only with Yaml format. Expected Encoder to be YamlOutput, got ${this::class}"
}
