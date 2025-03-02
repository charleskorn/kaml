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

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule

internal class YamlContextualInput(node: YamlNode, yaml: Yaml, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(node, yaml, context, configuration) {
    private val delegateIfScalar: YamlScalarInput? = when (node) {
        is YamlScalar -> YamlScalarInput(node, yaml, context, configuration)
        else -> null
    }

    override fun decodeString(): String = delegateToYamlScalarInput { decodeString() }
    override fun decodeInt(): Int = delegateToYamlScalarInput { decodeInt() }
    override fun decodeLong(): Long = delegateToYamlScalarInput { decodeLong() }
    override fun decodeShort(): Short = delegateToYamlScalarInput { decodeShort() }
    override fun decodeByte(): Byte = delegateToYamlScalarInput { decodeByte() }
    override fun decodeDouble(): Double = delegateToYamlScalarInput { decodeDouble() }
    override fun decodeFloat(): Float = delegateToYamlScalarInput { decodeFloat() }
    override fun decodeBoolean(): Boolean = delegateToYamlScalarInput { decodeBoolean() }
    override fun decodeChar(): Char = delegateToYamlScalarInput { decodeChar() }
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = delegateToYamlScalarInput { decodeEnum(enumDescriptor) }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = throw IllegalStateException("Must call beginStructure() and use returned Decoder")

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
        createFor(node, yaml, serializersModule, configuration, descriptor)

    override fun getCurrentLocation(): Location = node.location
    override fun getCurrentPath(): YamlPath = node.path

    private inline fun <T> delegateToYamlScalarInput(block: YamlScalarInput.() -> T): T {
        delegateIfScalar?.let { return it.block() }
        return when (node) {
            is YamlNull -> throw UnexpectedNullValueException(node.path)
            else -> throw IllegalStateException("Must call beginStructure() and use returned Decoder")
        }
    }
}
