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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal object LocationThrowingSerializer : KSerializer<Any> {
    override val descriptor = buildSerialDescriptor(LocationThrowingSerializer::class.simpleName!!, SerialKind.CONTEXTUAL)

    override fun deserialize(decoder: Decoder): Any {
        val location = (decoder as YamlInput).getCurrentLocation()
        val path = decoder.getCurrentPath()

        throw LocationInformationException("Serializer called with location (${location.line}, ${location.column}) and path: ${path.toHumanReadableString()}")
    }

    override fun serialize(encoder: Encoder, value: Any) = throw UnsupportedOperationException()
}

internal object LocationThrowingMapSerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = MapSerializer(String.serializer(), String.serializer()).descriptor

    override fun deserialize(decoder: Decoder): Any {
        val location = (decoder as YamlInput).getCurrentLocation()
        val path = decoder.getCurrentPath()

        throw LocationInformationException("Serializer called with location (${location.line}, ${location.column}) and path: ${path.toHumanReadableString()}")
    }

    override fun serialize(encoder: Encoder, value: Any) = throw UnsupportedOperationException()
}

internal class LocationInformationException(message: String) : RuntimeException(message)
