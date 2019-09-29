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

package com.charleskorn.kaml.testobjects

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.BooleanSerializer
import kotlinx.serialization.internal.ByteSerializer
import kotlinx.serialization.internal.CharSerializer
import kotlinx.serialization.internal.CommonEnumSerializer
import kotlinx.serialization.internal.DoubleSerializer
import kotlinx.serialization.internal.FloatSerializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.LongSerializer
import kotlinx.serialization.internal.ShortSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.internal.UnitSerializer
import kotlinx.serialization.internal.nullable
import kotlinx.serialization.modules.SerializersModule

sealed class TestSealedStructure {
    @Serializable
    @SerialName("sealedInt")
    data class SimpleSealedInt(val value: Int) : TestSealedStructure()

    @Serializable
    @SerialName("sealedString")
    data class SimpleSealedString(val value: String?) : TestSealedStructure()
}

@Serializable
data class SealedWrapper(@Polymorphic val element: TestSealedStructure?)

val sealedModule = SerializersModule {
    polymorphic(TestSealedStructure::class) {
        TestSealedStructure.SimpleSealedInt::class with TestSealedStructure.SimpleSealedInt.serializer()
        TestSealedStructure.SimpleSealedString::class with TestSealedStructure.SimpleSealedString.serializer()
    }
}

interface SimpleInterface

object SimpleNull : SimpleInterface {
    val kSerializer: KSerializer<SimpleNull> = UnitSerializer.nullable.mapped("simpleNull", { SimpleNull }, { null })
}

data class SimpleUnit(val data: Unit) : SimpleInterface {
    companion object {
        val kSerializer = UnitSerializer.mapped("simpleUnit", ::SimpleUnit, SimpleUnit::data)
    }
}

data class SimpleBoolean(val data: Boolean) : SimpleInterface {
    companion object {
        val kSerializer = BooleanSerializer.mapped("simpleBoolean", ::SimpleBoolean, SimpleBoolean::data)
    }
}

data class SimpleByte(val data: Byte) : SimpleInterface {
    companion object {
        val kSerializer = ByteSerializer.mapped("simpleByte", ::SimpleByte, SimpleByte::data)
    }
}

data class SimpleShort(val data: Short) : SimpleInterface {
    companion object {
        val kSerializer = ShortSerializer.mapped("simpleShort", ::SimpleShort, SimpleShort::data)
    }
}

data class SimpleInt(val data: Int) : SimpleInterface {
    companion object {
        val kSerializer = IntSerializer.mapped("simpleInt", ::SimpleInt, SimpleInt::data)
    }
}

data class SimpleNullableInt(val data: Int?) : SimpleInterface {
    companion object {
        val kSerializer = IntSerializer.nullable.mapped("simpleNullableInt", ::SimpleNullableInt, SimpleNullableInt::data)
    }
}

data class SimpleLong(val data: Long) : SimpleInterface {
    companion object {
        val kSerializer = LongSerializer.mapped("simpleLong", ::SimpleLong, SimpleLong::data)
    }
}

data class SimpleFloat(val data: Float) : SimpleInterface {
    companion object {
        val kSerializer = FloatSerializer.mapped("simpleFloat", ::SimpleFloat, SimpleFloat::data)
    }
}

data class SimpleDouble(val data: Double) : SimpleInterface {
    companion object {
        val kSerializer = DoubleSerializer.mapped("simpleDouble", ::SimpleDouble, SimpleDouble::data)
    }
}

data class SimpleChar(val data: Char) : SimpleInterface {
    companion object {
        val kSerializer = CharSerializer.mapped("simpleChar", ::SimpleChar, SimpleChar::data)
    }
}

data class SimpleString(val data: String) : SimpleInterface {
    companion object {
        val kSerializer = StringSerializer.mapped("simpleString", ::SimpleString, SimpleString::data)
    }
}

enum class SimpleEnum : SimpleInterface {
    TEST, TEST2;

    companion object {
        val kSerializer = CommonEnumSerializer("simpleEnum", values(), values().map(SimpleEnum::name).toTypedArray())
    }
}

fun SerialDescriptor.withName(newName: String): SerialDescriptor {
    return object : SerialDescriptor by this {
        override val name: String = newName
    }
}

inline fun <S, T> KSerializer<S>.mapped(descriptorName: String = descriptor.name, crossinline fromSource: (S) -> T, crossinline toSource: (T) -> S): KSerializer<T> {
    return object : KSerializer<T> {
        override val descriptor: SerialDescriptor = this@mapped.descriptor.withName(descriptorName)

        override fun deserialize(decoder: Decoder): T = fromSource(this@mapped.deserialize(decoder))

        override fun serialize(encoder: Encoder, obj: T) = this@mapped.serialize(encoder, toSource(obj))
    }
}

@Serializable
data class SimpleWrapper(val test: SimpleInterface)

val simpleModule = SerializersModule {
    polymorphic(SimpleInterface::class) {
        SimpleNull::class with SimpleNull.kSerializer
        SimpleUnit::class with SimpleUnit.kSerializer
        SimpleBoolean::class with SimpleBoolean.kSerializer
        SimpleByte::class with SimpleByte.kSerializer
        SimpleShort::class with SimpleShort.kSerializer
        SimpleInt::class with SimpleInt.kSerializer
        SimpleLong::class with SimpleLong.kSerializer
        SimpleFloat::class with SimpleFloat.kSerializer
        SimpleDouble::class with SimpleDouble.kSerializer
        SimpleChar::class with SimpleChar.kSerializer
        SimpleString::class with SimpleString.kSerializer
        SimpleEnum::class with SimpleEnum.kSerializer
        SimpleNullableInt::class with SimpleNullableInt.kSerializer
    }
}
