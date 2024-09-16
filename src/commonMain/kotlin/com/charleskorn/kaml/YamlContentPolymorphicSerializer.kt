package com.charleskorn.kaml

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
public abstract class YamlContentPolymorphicSerializer<T : Any>(private val baseClass: KClass<T>) : KSerializer<T> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor(
        "${YamlContentPolymorphicSerializer::class.simpleName}<${baseClass.simpleName}>",
        PolymorphicKind.SEALED
    )

    @OptIn(InternalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: T) {
        val actualSerializer = encoder.serializersModule.getPolymorphic(baseClass, value)
            ?: value::class.serializerOrNull()
            ?: throwSubtypeNotRegistered(value::class, baseClass)
        @Suppress("UNCHECKED_CAST")
        (actualSerializer as KSerializer<T>).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): T {
        return decoder.decodeSerializableValue(selectDeserializer((decoder as YamlInput).node))
    }

    public abstract fun selectDeserializer(node: YamlNode): DeserializationStrategy<T>

    private fun throwSubtypeNotRegistered(subClass: KClass<*>, baseClass: KClass<*>): Nothing {
        val subClassName = subClass.simpleName ?: "$subClass"
        throw SerializationException("""
            Class '${subClassName}' is not registered for polymorphic serialization in the scope of '${baseClass.simpleName}'.
            Mark the base class as 'sealed' or register the serializer explicitly.
        """.trimIndent())
    }
}
