/*

   Copyright 2018-2019 Charles Korn.

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

package com.charleskorn.kaml

import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.ElementValueEncoder
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StructureKind
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.SerialModule
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.emitter.Emitter
import org.snakeyaml.engine.v2.events.DocumentStartEvent
import org.snakeyaml.engine.v2.events.ImplicitTuple
import org.snakeyaml.engine.v2.events.MappingEndEvent
import org.snakeyaml.engine.v2.events.MappingStartEvent
import org.snakeyaml.engine.v2.events.ScalarEvent
import org.snakeyaml.engine.v2.events.SequenceEndEvent
import org.snakeyaml.engine.v2.events.SequenceStartEvent
import org.snakeyaml.engine.v2.events.StreamStartEvent
import java.util.Optional

internal class YamlOutput(
    writer: StreamDataWriter,
    override val context: SerialModule,
    private val configuration: YamlConfiguration
) : ElementValueEncoder() {
    private val settings = DumpSettings.builder().build()
    private val emitter = Emitter(settings, writer)
    private var currentTag: String? = null

    init {
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(false, Optional.empty(), emptyMap()))
    }

    override fun shouldEncodeElementDefault(desc: SerialDescriptor, index: Int): Boolean = configuration.encodeDefaults
    override fun encodeNull() = emitPlainScalar("null")
    override fun encodeBoolean(value: Boolean) = emitPlainScalar(value.toString())
    override fun encodeByte(value: Byte) = emitPlainScalar(value.toString())
    override fun encodeChar(value: Char) = emitQuotedScalar(value.toString())
    override fun encodeDouble(value: Double) = emitPlainScalar(value.toString())
    override fun encodeFloat(value: Float) = emitPlainScalar(value.toString())
    override fun encodeInt(value: Int) = emitPlainScalar(value.toString())
    override fun encodeLong(value: Long) = emitPlainScalar(value.toString())
    override fun encodeShort(value: Short) = emitPlainScalar(value.toString())
    override fun encodeString(value: String) = emitQuotedScalar(value)
    override fun encodeEnum(enumDescription: SerialDescriptor, ordinal: Int) = emitQuotedScalar(enumDescription.getElementName(ordinal))

    private fun emitPlainScalar(value: String) = emitScalar(value, ScalarStyle.PLAIN)
    private fun emitQuotedScalar(value: String) = emitScalar(value, ScalarStyle.DOUBLE_QUOTED)

    override fun encodeElement(desc: SerialDescriptor, index: Int): Boolean {
        if (desc.kind is StructureKind.CLASS) {
            emitPlainScalar(desc.getElementName(index))
        }

        return super.encodeElement(desc, index)
    }

    @UseExperimental(InternalSerializationApi::class)
    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        if (serializer !is AbstractPolymorphicSerializer<*>) {
            serializer.serialize(this, value)
            return
        }

        @Suppress("UNCHECKED_CAST")
        val actualSerializer = (serializer as AbstractPolymorphicSerializer<Any>).findPolymorphicSerializer(this, value as Any) as KSerializer<Any>
        currentTag = actualSerializer.descriptor.name
        actualSerializer.serialize(this, value)
    }

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder {
        val tag = getAndClearTag()
        val implicit = !tag.isPresent
        when (desc.kind) {
            StructureKind.LIST -> emitter.emit(SequenceStartEvent(Optional.empty(), tag, implicit, FlowStyle.BLOCK))
            StructureKind.MAP, StructureKind.CLASS, PrimitiveKind.UNIT -> emitter.emit(MappingStartEvent(Optional.empty(), tag, implicit, FlowStyle.BLOCK))
        }

        return super.beginStructure(desc, *typeParams)
    }

    override fun endStructure(desc: SerialDescriptor) {
        when (desc.kind) {
            StructureKind.LIST -> emitter.emit(SequenceEndEvent())
            StructureKind.MAP, StructureKind.CLASS, PrimitiveKind.UNIT -> emitter.emit(MappingEndEvent())
        }

        super.endStructure(desc)
    }

    private fun emitScalar(value: String, style: ScalarStyle) {
        val tag = getAndClearTag()
        val implicit = if (tag.isPresent) ALL_EXPLICIT else ALL_IMPLICIT
        emitter.emit(ScalarEvent(Optional.empty(), tag, implicit, value, style))
    }

    private fun getAndClearTag(): Optional<String> {
        val tag = Optional.ofNullable(currentTag)
        currentTag = null
        return tag
    }

    companion object {
        private val ALL_IMPLICIT = ImplicitTuple(true, true)
        private val ALL_EXPLICIT = ImplicitTuple(false, false)
    }
}
