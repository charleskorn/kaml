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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlinx.serialization.internal.EnumDescriptor
import org.snakeyaml.engine.v1.api.DumpSettingsBuilder
import org.snakeyaml.engine.v1.api.StreamDataWriter
import org.snakeyaml.engine.v1.common.FlowStyle
import org.snakeyaml.engine.v1.common.ScalarStyle
import org.snakeyaml.engine.v1.emitter.Emitter
import org.snakeyaml.engine.v1.events.DocumentStartEvent
import org.snakeyaml.engine.v1.events.ImplicitTuple
import org.snakeyaml.engine.v1.events.MappingEndEvent
import org.snakeyaml.engine.v1.events.MappingStartEvent
import org.snakeyaml.engine.v1.events.ScalarEvent
import org.snakeyaml.engine.v1.events.SequenceEndEvent
import org.snakeyaml.engine.v1.events.SequenceStartEvent
import org.snakeyaml.engine.v1.events.StreamStartEvent
import java.util.Optional

internal class YamlOutput(writer: StreamDataWriter) : ElementValueEncoder() {
    private val settings = DumpSettingsBuilder().build()
    private val emitter = Emitter(settings, writer)

    init {
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(false, Optional.empty(), emptyMap()))
    }

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
    override fun encodeEnum(enumDescription: EnumDescriptor, ordinal: Int) = emitQuotedScalar(enumDescription.getElementName(ordinal))

    private fun emitPlainScalar(value: String) = emitScalar(value, ScalarStyle.PLAIN)
    private fun emitQuotedScalar(value: String) = emitScalar(value, ScalarStyle.DOUBLE_QUOTED)

    override fun encodeElement(desc: SerialDescriptor, index: Int): Boolean {
        if (desc.kind is StructureKind.CLASS) {
            emitScalar(desc.getElementName(index), ScalarStyle.PLAIN)
        }

        return super.encodeElement(desc, index)
    }

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder {
        when (desc.kind) {
            is StructureKind.LIST -> emitter.emit(SequenceStartEvent(Optional.empty(), Optional.empty(), true, FlowStyle.BLOCK))
            is StructureKind.MAP, StructureKind.CLASS -> emitter.emit(MappingStartEvent(Optional.empty(), Optional.empty(), true, FlowStyle.BLOCK))
        }

        return super.beginStructure(desc, *typeParams)
    }

    override fun endStructure(desc: SerialDescriptor) {
        when (desc.kind) {
            is StructureKind.LIST -> emitter.emit(SequenceEndEvent())
            is StructureKind.MAP, StructureKind.CLASS -> emitter.emit(MappingEndEvent())
        }

        super.endStructure(desc)
    }

    private fun emitScalar(value: String, style: ScalarStyle) =
        emitter.emit(ScalarEvent(Optional.empty(), Optional.empty(), ImplicitTuple(true, true), value, style))
}
