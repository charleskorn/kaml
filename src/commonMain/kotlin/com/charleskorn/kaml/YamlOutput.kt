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

import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentType
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.events.CommentEvent
import it.krzeminski.snakeyaml.engine.kmp.events.DocumentEndEvent
import it.krzeminski.snakeyaml.engine.kmp.events.DocumentStartEvent
import it.krzeminski.snakeyaml.engine.kmp.events.ImplicitTuple
import it.krzeminski.snakeyaml.engine.kmp.events.MappingEndEvent
import it.krzeminski.snakeyaml.engine.kmp.events.MappingStartEvent
import it.krzeminski.snakeyaml.engine.kmp.events.ScalarEvent
import it.krzeminski.snakeyaml.engine.kmp.events.SequenceEndEvent
import it.krzeminski.snakeyaml.engine.kmp.events.SequenceStartEvent
import it.krzeminski.snakeyaml.engine.kmp.events.StreamEndEvent
import it.krzeminski.snakeyaml.engine.kmp.events.StreamStartEvent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class, ExperimentalStdlibApi::class)
internal class YamlOutput(
    writer: StreamDataWriter,
    override val serializersModule: SerializersModule,
    private val configuration: YamlConfiguration,
) : AbstractEncoder(), AutoCloseable {
    private val settings = DumpSettings.builder()
        .setDumpComments(true)
        // SnakeYAML validates that this value must be at least 1
        .setIndent(configuration.encodingIndentationSize)
        // SnakeYAML helps to validate that this value must be non-negative
        .setIndicatorIndent(configuration.sequenceBlockIndent)
        // No special reason why true is conditional. Designed to be consistent with 0.46.0 of kaml
        .setIndentWithIndicator(configuration.sequenceBlockIndent > 0)
        // Unclear if this value is validated
        .setWidth(configuration.breakScalarsAt)
        .build()

    private val emitter = Emitter(settings, writer)
    private var shouldReadTypeName = false
    private var currentTypeName: String? = null

    init {
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(false, null, emptyMap()))
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
                configuration.singleLineStringStyle == SingleLineStringStyle.PlainExceptAmbiguous && value.isAmbiguous() -> emitQuotedScalar(value, configuration.ambiguousQuoteStyle.scalarStyle)
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
            val elementName = descriptor.getElementName(index)
            val serializedName = configuration.yamlNamingStrategy?.serialNameForYaml(elementName) ?: elementName
            emitPlainScalar(serializedName)
        }

        return super.encodeElement(descriptor, index)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        when (descriptor.kind) {
            is PolymorphicKind -> shouldReadTypeName = true
            StructureKind.LIST -> emitter.emit(SequenceStartEvent(null, null, true, configuration.sequenceStyle.flowStyle))
            StructureKind.MAP, StructureKind.CLASS, StructureKind.OBJECT -> {
                val typeName = getAndClearTypeName()

                when (configuration.polymorphismStyle) {
                    PolymorphismStyle.Tag -> {
                        val implicit = typeName == null
                        emitter.emit(MappingStartEvent(null, typeName, implicit, FlowStyle.BLOCK))
                    }
                    PolymorphismStyle.Property -> {
                        emitter.emit(MappingStartEvent(null, null, true, FlowStyle.BLOCK))

                        if (typeName != null) {
                            emitPlainScalar(configuration.polymorphismPropertyName)
                            emitQuotedScalar(typeName, SingleLineStringStyle.DoubleQuoted.scalarStyle)
                        }
                    }
                    PolymorphismStyle.None -> {
                        emitter.emit(MappingStartEvent(null, null, true, FlowStyle.BLOCK))
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
            .firstOrNull() ?: return

        for (line in commentAnno.lines) {
            emitter.emit(CommentEvent(CommentType.BLOCK, " $line", null, null))
        }
    }

    private fun emitScalar(value: String, style: ScalarStyle) {
        val tag = getAndClearTypeName()

        if (tag != null && configuration.polymorphismStyle != PolymorphismStyle.Tag) {
            throw IllegalStateException("Cannot serialize a polymorphic value that is not a YAML object when using ${PolymorphismStyle::class.simpleName}.${configuration.polymorphismStyle}.")
        }

        val implicit = if (tag != null) ALL_EXPLICIT else ALL_IMPLICIT
        emitter.emit(ScalarEvent(null, tag, implicit, value, style))
    }

    private fun getAndClearTypeName(): String? {
        val typeName = currentTypeName
        currentTypeName = null
        return typeName
    }

    private fun String.isAmbiguous(): Boolean {
        return when {
            isEmpty() -> true
            startsWith("0x") -> true
            startsWith("0o") -> true
            toDoubleOrNull() != null -> true
            startsWith("#") -> true
            else -> this in listOf(
                "~", "-", ".inf", ".Inf", ".INF", "-.inf", "-.Inf", "-.INF", ".nan", ".NaN", ".NAN", "-.nan", "-.NaN",
                "-.NAN", "null", "Null", "NULL", "true", "True", "TRUE", "false", "False", "FALSE",
            )
        }
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
            SingleLineStringStyle.PlainExceptAmbiguous -> ScalarStyle.PLAIN
        }

    private val AmbiguousQuoteStyle.scalarStyle: ScalarStyle
        get() = when (this) {
            AmbiguousQuoteStyle.DoubleQuoted -> ScalarStyle.DOUBLE_QUOTED
            AmbiguousQuoteStyle.SingleQuoted -> ScalarStyle.SINGLE_QUOTED
        }

    companion object {
        private val ALL_IMPLICIT = ImplicitTuple(true, true)
        private val ALL_EXPLICIT = ImplicitTuple(false, false)
    }
}
