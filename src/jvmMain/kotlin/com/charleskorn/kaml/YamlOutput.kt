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

package com.charleskorn.kaml

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.emitter.Emitter
import org.snakeyaml.engine.v2.events.CommentEvent
import org.snakeyaml.engine.v2.events.DocumentEndEvent
import org.snakeyaml.engine.v2.events.DocumentStartEvent
import org.snakeyaml.engine.v2.events.ImplicitTuple
import org.snakeyaml.engine.v2.events.MappingEndEvent
import org.snakeyaml.engine.v2.events.MappingStartEvent
import org.snakeyaml.engine.v2.events.ScalarEvent
import org.snakeyaml.engine.v2.events.SequenceEndEvent
import org.snakeyaml.engine.v2.events.SequenceStartEvent
import org.snakeyaml.engine.v2.events.StreamEndEvent
import org.snakeyaml.engine.v2.events.StreamStartEvent
import java.util.Optional

@OptIn(ExperimentalSerializationApi::class)
internal class YamlOutput(
    writer: StreamDataWriter,
    override val serializersModule: SerializersModule,
    private val configuration: YamlConfiguration
) : AbstractEncoder(), AutoCloseable {
    private val settings = DumpSettings.builder()
        .setIndent(configuration.encodingIndentationSize)
        .setWidth(configuration.breakScalarsAt)
        .build()

    private val emitter = Emitter(settings, writer)
    private var shouldReadTypeName = false
    private var currentTypeName: String? = null

    init {
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(false, Optional.empty(), emptyMap()))
    }

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean = configuration.encodeDefaults
    override fun encodeNull() = emitPlainScalar("null")
    override fun encodeBoolean(value: Boolean) = emitPlainScalar(value.toString())
    override fun encodeByte(value: Byte) = emitPlainScalar(value.toString())
    override fun encodeChar(value: Char) = emitQuotedScalar(value.toString(), configuration.singleLineStringStyle.scalarStyle)
    override fun encodeDouble(value: Double) = emitPlainScalar(value.toString())
    override fun encodeFloat(value: Float) = emitPlainScalar(value.toString())
    override fun encodeInt(value: Int) = emitPlainScalar(value.toString())
    override fun encodeLong(value: Long) = emitPlainScalar(value.toString())
    override fun encodeShort(value: Short) = emitPlainScalar(value.toString())
    override fun encodeString(value: String) {
        if (shouldReadTypeName) {
            currentTypeName = value
            shouldReadTypeName = false
        } else {
            when {
                value.contains('\n') -> emitQuotedScalar(value, configuration.multiLineStringStyle.scalarStyle)
                else -> emitQuotedScalar(value, configuration.singleLineStringStyle.scalarStyle)
            }
        }
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = emitQuotedScalar(enumDescriptor.getElementName(index), configuration.singleLineStringStyle.scalarStyle)

    private fun emitPlainScalar(value: String) = emitScalar(value, ScalarStyle.PLAIN)
    private fun emitQuotedScalar(value: String, scalarStyle: ScalarStyle) = emitScalar(value, scalarStyle)

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        encodeComment(descriptor, index)

        if (descriptor.kind is StructureKind.CLASS) {
            emitPlainScalar(descriptor.getElementName(index))
        }

        return super.encodeElement(descriptor, index)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        when (descriptor.kind) {
            is PolymorphicKind -> shouldReadTypeName = true
            StructureKind.LIST -> emitter.emit(SequenceStartEvent(Optional.empty(), Optional.empty(), true, configuration.sequenceStyle.flowStyle))
            StructureKind.MAP, StructureKind.CLASS, StructureKind.OBJECT -> {
                val typeName = getAndClearTypeName()

                when (configuration.polymorphismStyle) {
                    PolymorphismStyle.Tag -> {
                        val implicit = !typeName.isPresent
                        emitter.emit(MappingStartEvent(Optional.empty(), typeName, implicit, FlowStyle.BLOCK))
                    }
                    PolymorphismStyle.Property -> {
                        emitter.emit(MappingStartEvent(Optional.empty(), Optional.empty(), true, FlowStyle.BLOCK))

                        if (typeName.isPresent) {
                            emitPlainScalar(configuration.polymorphismPropertyName)
                            emitQuotedScalar(typeName.get(), SingleLineStringStyle.DoubleQuoted.scalarStyle)
                        }
                    }
                }
            }
            else -> {
                // Nothing to do.
            }
        }

        return super.beginStructure(descriptor)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        when (descriptor.kind) {
            StructureKind.LIST -> emitter.emit(SequenceEndEvent())
            StructureKind.MAP, StructureKind.CLASS, StructureKind.OBJECT -> emitter.emit(MappingEndEvent())
            else -> {
                // Nothing to do.
            }
        }
    }

    override fun close() {
        emitter.emit(DocumentEndEvent(false))
        emitter.emit(StreamEndEvent())
    }

    private fun encodeComment(descriptor: SerialDescriptor, index: Int) {
        val commentAnno = descriptor.getElementAnnotations(index)
            .filterIsInstance<YamlComment>()
            .firstOrNull()

        if (commentAnno == null) {
            return
        }
        
        for (line in commentAnno.lines) {
            emitter.emit(CommentEvent(CommentType.BLOCK, " $line", Optional.empty(), Optional.empty()))
        }
    }

    private fun emitScalar(value: String, style: ScalarStyle) {
        val tag = getAndClearTypeName()

        if (tag.isPresent && configuration.polymorphismStyle != PolymorphismStyle.Tag) {
            throw IllegalStateException("Cannot serialize a polymorphic value that is not a YAML object when using ${PolymorphismStyle::class.simpleName}.${configuration.polymorphismStyle}.")
        }

        val implicit = if (tag.isPresent) ALL_EXPLICIT else ALL_IMPLICIT
        emitter.emit(ScalarEvent(Optional.empty(), tag, implicit, value, style))
    }

    private fun getAndClearTypeName(): Optional<String> {
        val typeName = Optional.ofNullable(currentTypeName)
        currentTypeName = null
        return typeName
    }

    private val SequenceStyle.flowStyle: FlowStyle
        get() = when (this) {
            SequenceStyle.Block -> FlowStyle.BLOCK
            SequenceStyle.Flow -> FlowStyle.FLOW
        }

    private val MultiLineStringStyle.scalarStyle: ScalarStyle
        get() = when (this) {
            MultiLineStringStyle.DoubleQuoted -> ScalarStyle.DOUBLE_QUOTED
            MultiLineStringStyle.SingleQuoted -> ScalarStyle.SINGLE_QUOTED
            MultiLineStringStyle.Literal -> ScalarStyle.LITERAL
            MultiLineStringStyle.Plain -> ScalarStyle.PLAIN
        }

    private val SingleLineStringStyle.scalarStyle: ScalarStyle
        get() = when (this) {
            SingleLineStringStyle.DoubleQuoted -> ScalarStyle.DOUBLE_QUOTED
            SingleLineStringStyle.SingleQuoted -> ScalarStyle.SINGLE_QUOTED
            SingleLineStringStyle.Plain -> ScalarStyle.PLAIN
        }

    companion object {
        private val ALL_IMPLICIT = ImplicitTuple(true, true)
        private val ALL_EXPLICIT = ImplicitTuple(false, false)
    }
}
