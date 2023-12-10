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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
internal class YamlPolymorphicInput(private val typeName: String, private val typeNamePath: YamlPath, private val contentNode: YamlNode, yaml: Yaml, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(contentNode, yaml, context, configuration) {
    private var currentField = CurrentField.NotStarted
    private lateinit var contentDecoder: YamlInput

    override fun getCurrentLocation(): Location = contentNode.location
    override fun getCurrentPath(): YamlPath = contentNode.path

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return when (currentField) {
            CurrentField.NotStarted -> {
                currentField = CurrentField.Type
                0
            }
            CurrentField.Type -> {
                when (contentNode) {
                    is YamlScalar -> contentDecoder = YamlScalarInput(contentNode, yaml, serializersModule, configuration)
                    is YamlNull -> contentDecoder = YamlNullInput(contentNode, yaml, serializersModule, configuration)
                    else -> {
                        // Nothing to do here - contentDecoder is set in beginStructure() for non-scalar values.
                    }
                }

                currentField = CurrentField.Content
                1
            }
            CurrentField.Content -> CompositeDecoder.DECODE_DONE
        }
    }

    override fun decodeNotNullMark(): Boolean = maybeCallOnContent(blockOnType = { true }, blockOnContent = YamlInput::decodeNotNullMark)
    override fun decodeNull(): Nothing? = maybeCallOnContent("decodeNull", blockOnContent = YamlInput::decodeNull)
    override fun decodeBoolean(): Boolean = maybeCallOnContent("decodeBoolean", blockOnContent = YamlInput::decodeBoolean)
    override fun decodeByte(): Byte = maybeCallOnContent("decodeByte", blockOnContent = YamlInput::decodeByte)
    override fun decodeShort(): Short = maybeCallOnContent("decodeShort", blockOnContent = YamlInput::decodeShort)
    override fun decodeInt(): Int = maybeCallOnContent("decodeInt", blockOnContent = YamlInput::decodeInt)
    override fun decodeLong(): Long = maybeCallOnContent("decodeLong", blockOnContent = YamlInput::decodeLong)
    override fun decodeFloat(): Float = maybeCallOnContent("decodeFloat", blockOnContent = YamlInput::decodeFloat)
    override fun decodeDouble(): Double = maybeCallOnContent("decodeDouble", blockOnContent = YamlInput::decodeDouble)
    override fun decodeChar(): Char = maybeCallOnContent("decodeChar", blockOnContent = YamlInput::decodeChar)
    override fun decodeString(): String = maybeCallOnContent(blockOnType = { typeName }, blockOnContent = YamlInput::decodeString)
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = maybeCallOnContent("decodeEnum") { decodeEnum(enumDescriptor) }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (currentField) {
            CurrentField.NotStarted, CurrentField.Type -> super.beginStructure(descriptor)
            CurrentField.Content -> {
                contentDecoder = createFor(contentNode, yaml, serializersModule, configuration, descriptor)

                return contentDecoder
            }
        }
    }

    private inline fun <T> maybeCallOnContent(functionName: String, blockOnContent: YamlInput.() -> T): T =
        maybeCallOnContent(blockOnType = { throw UnsupportedOperationException("Can't call $functionName() on type field") }, blockOnContent = blockOnContent)

    private inline fun <T> maybeCallOnContent(blockOnType: () -> T, blockOnContent: YamlInput.() -> T): T {
        return when (currentField) {
            CurrentField.NotStarted, CurrentField.Type -> blockOnType()
            CurrentField.Content -> contentDecoder.blockOnContent()
        }
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        try {
            return super.decodeSerializableValue(deserializer)
        } catch (e: SerializationException) {
            throwIfUnknownPolymorphicTypeException(e, deserializer)

            throw e
        }
    }

    private fun throwIfUnknownPolymorphicTypeException(e: Exception, deserializer: DeserializationStrategy<*>) {
        val message = e.message ?: return
        val match = unknownPolymorphicTypeExceptionMessage.matchAt(message, 0) ?: return
        val unknownType = match.groupValues[1]
        val className = match.groupValues[2]

        val knownTypes = when (deserializer.descriptor.kind) {
            PolymorphicKind.SEALED -> getKnownTypesForSealedType(deserializer)
            PolymorphicKind.OPEN -> getKnownTypesForOpenType(className)
            else -> throw IllegalArgumentException("Can't get known types for descriptor of kind ${deserializer.descriptor.kind}")
        }

        throw UnknownPolymorphicTypeException(unknownType, knownTypes, typeNamePath, e)
    }

    private fun getKnownTypesForSealedType(deserializer: DeserializationStrategy<*>): Set<String> {
        val typesDescriptor = deserializer.descriptor.getElementDescriptor(1)

        return typesDescriptor.elementNames.toSet()
    }

    private fun getKnownTypesForOpenType(className: String): Set<String> {
        val knownTypes = mutableSetOf<String>()

        serializersModule.dumpTo(object : SerializersModuleCollector {
            override fun <T : Any> contextual(kClass: KClass<T>, provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>) {}

            // FIXME: ideally we'd be able to get the name as used by the SerialModule (eg. the values in 'polyBase2NamedSerializers' in SerialModuleImpl, but these aren't exposed.
            // The serializer's descriptor's name seems to be the same value.
            override fun <Base : Any, Sub : Base> polymorphic(baseClass: KClass<Base>, actualClass: KClass<Sub>, actualSerializer: KSerializer<Sub>) {
                if (baseClass.simpleName == className) {
                    knownTypes.add(actualSerializer.descriptor.serialName)
                }
            }

            @ExperimentalSerializationApi
            override fun <Base : Any> polymorphicDefaultSerializer(baseClass: KClass<Base>, defaultSerializerProvider: (value: Base) -> SerializationStrategy<Base>?) {
                throw UnsupportedOperationException("This method should never be called.")
            }

            @ExperimentalSerializationApi
            override fun <Base : Any> polymorphicDefaultDeserializer(baseClass: KClass<Base>, defaultDeserializerProvider: (className: String?) -> DeserializationStrategy<Base>?) {
                throw UnsupportedOperationException("This method should never be called")
            }
        })

        return knownTypes
    }

    private enum class CurrentField {
        NotStarted,
        Type,
        Content,
    }

    companion object {
        private val unknownPolymorphicTypeExceptionMessage: Regex = """^Serializer for subclass '(.*)' is not found in the polymorphic scope of '(.*)'.\n.*""".toRegex()
    }
}
