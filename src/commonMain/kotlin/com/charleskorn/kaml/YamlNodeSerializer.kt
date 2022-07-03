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

@file:OptIn(ExperimentalSerializationApi::class)

package com.charleskorn.kaml

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private fun defer(deferred: () -> SerialDescriptor): SerialDescriptor = object : SerialDescriptor {

    private val original: SerialDescriptor by lazy(deferred)

    override val serialName: String
        get() = original.serialName
    override val kind: SerialKind
        get() = original.kind
    override val elementsCount: Int
        get() = original.elementsCount

    override fun getElementName(index: Int): String = original.getElementName(index)
    override fun getElementIndex(name: String): Int = original.getElementIndex(name)
    override fun getElementAnnotations(index: Int): List<Annotation> = original.getElementAnnotations(index)
    override fun getElementDescriptor(index: Int): SerialDescriptor = original.getElementDescriptor(index)
    override fun isElementOptional(index: Int): Boolean = original.isElementOptional(index)
}

private val Decoder.currentPath: YamlPath
    get() {
        return if (this is YamlInput) {
            getCurrentPath()
        } else {
            // TODO not sure if this is a good idea or if it should just fail
            YamlPath.root
        }
    }

@Serializer(forClass = YamlNode::class)
public object YamlNodeSerializer : KSerializer<YamlNode> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.charleskorn.kaml.YamlNode") {
        // Resolve cyclic dependency in descriptors by late binding
        element("YamlScalar", defer { YamlScalarSerializer.descriptor })
        element("YamlNull", defer { YamlNullSerializer.descriptor })
        element("YamlMap", defer { YamlMapSerializer.descriptor })
        element("YamlList", defer { YamlListSerializer.descriptor })
        element("YamlTaggedNode", defer { YamlTaggedNodeSerializer.descriptor })
    }

    override fun deserialize(decoder: Decoder): YamlNode {
        return (decoder as YamlInput).node
    }

    override fun serialize(encoder: Encoder, value: YamlNode) {
        when (value) {
            is YamlList -> YamlListSerializer.serialize(encoder, value)
            is YamlMap -> YamlMapSerializer.serialize(encoder, value)
            is YamlNull -> YamlNullSerializer.serialize(encoder, value)
            is YamlScalar -> YamlScalarSerializer.serialize(encoder, value)
            is YamlTaggedNode -> YamlTaggedNodeSerializer.serialize(encoder, value)
        }
    }
}

@Serializer(forClass = YamlTaggedNode::class)
public object YamlTaggedNodeSerializer : KSerializer<YamlTaggedNode> {

    // TODO this has practically no support for tags...

    private object YamlTaggedNodeDescriptor : SerialDescriptor by YamlNodeSerializer.descriptor {
        override val serialName: String = "com.charleskorn.kaml.YamlTaggedNode"
    }

    override val descriptor: SerialDescriptor = YamlTaggedNodeDescriptor
    override fun deserialize(decoder: Decoder): YamlTaggedNode {
        return YamlTaggedNode("", YamlNodeSerializer.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: YamlTaggedNode) {
        YamlNodeSerializer.serialize(encoder, value.innerNode)
    }
}

@Serializer(forClass = YamlNull::class)
public object YamlNullSerializer : KSerializer<YamlNull> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.charleskorn.kaml.YamlNull", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): YamlNull {
        if (decoder is YamlNullInput) {
            return decoder.nullValue
        }
        decoder.decodeNull()
        return YamlNull(decoder.currentPath)
    }

    override fun serialize(encoder: Encoder, value: YamlNull) {
        encoder.encodeNull()
    }
}

@Serializer(forClass = YamlScalar::class)
public object YamlScalarSerializer : KSerializer<YamlScalar> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.charleskorn.kaml.YamlScalar", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): YamlScalar {
        if (decoder is YamlScalarInput) {
            return decoder.scalar
        }
        println("########## ${decoder::class.java.name}")
        return (decoder as YamlInput).node as YamlScalar
    }

    override fun serialize(encoder: Encoder, value: YamlScalar) {
        encoder as YamlOutput
        encoder.emitPlainScalar(value.content)
    }
}

@Serializer(forClass = YamlList::class)
public object YamlListSerializer : KSerializer<YamlList> {
    private val listSerializer = ListSerializer(YamlNodeSerializer)

    private object YamlMapDescriptor :
        SerialDescriptor by listSerializer.descriptor {
        override val serialName: String = "com.charleskorn.kaml.YamlList"
    }

    override val descriptor: SerialDescriptor = YamlMapDescriptor

    override fun deserialize(decoder: Decoder): YamlList {
        if (decoder is YamlListInput) {
            return decoder.list
        }
        return YamlList(listSerializer.deserialize(decoder), decoder.currentPath)
    }

    override fun serialize(encoder: Encoder, value: YamlList) {
        return listSerializer.serialize(encoder, value.items)
    }
}

@Serializer(forClass = YamlMap::class)
public object YamlMapSerializer : KSerializer<YamlMap> {
    private val mapSerializer = MapSerializer(YamlScalarSerializer, YamlNodeSerializer)

    private object YamlMapDescriptor :
        SerialDescriptor by mapSerializer.descriptor {
        override val serialName: String = "com.charleskorn.kaml.YamlMap"
    }

    override val descriptor: SerialDescriptor = YamlMapDescriptor

    override fun deserialize(decoder: Decoder): YamlMap {
        if (decoder is YamlMapInput) {
            return decoder.map
        }
        return YamlMap(mapSerializer.deserialize(decoder), decoder.currentPath)
    }

    override fun serialize(encoder: Encoder, value: YamlMap) {
        return mapSerializer.serialize(encoder, value.entries)
    }
}
