package com.charleskorn.kaml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public abstract class YamlTransformingSerializer<T : Any?>(
    private val tSerializer: KSerializer<T>
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
