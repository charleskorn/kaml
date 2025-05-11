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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public abstract class YamlTransformingSerializer<T : Any?>(
    private val tSerializer: KSerializer<T>,
) : KSerializer<T> {

    override val descriptor: SerialDescriptor get() = tSerializer.descriptor

    final override fun serialize(encoder: Encoder, value: T) {
        val encoder = encoder.asYamlOutput()
        val node = encoder.yaml.encodeToYamlNode(tSerializer, value)
        val transformedNode = transformSerialize(node)
        encoder.encodeSerializableValue(YamlNodeSerializer, transformedNode)
    }

    final override fun deserialize(decoder: Decoder): T {
        val decoder = decoder.asYamlInput<YamlInput>()
        val transformedNode = transformDeserialize(decoder.node)
        val value = decoder.yaml.decodeFromYamlNode(tSerializer, transformedNode)
        return value
    }

    protected open fun transformDeserialize(element: YamlNode): YamlNode = element

    protected open fun transformSerialize(element: YamlNode): YamlNode = element
}
