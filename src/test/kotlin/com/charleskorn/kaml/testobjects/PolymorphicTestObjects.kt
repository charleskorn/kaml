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
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.UnitSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule

@Serializable
sealed class TestSealedStructure {
    @Serializable
    @SerialName("sealedInt")
    data class SimpleSealedInt(val value: Int) : TestSealedStructure()

    @Serializable
    @SerialName("sealedString")
    data class SimpleSealedString(val value: String?) : TestSealedStructure()
}

@Serializable
data class SealedWrapper(val element: TestSealedStructure?)

interface SimpleInterface

object SimpleNull : SimpleInterface {
    val kSerializer: KSerializer<SimpleNull> = UnwrappedValueSerializer(UnitSerializer().nullable, "simpleNull", { SimpleNull }, { null })
}

data class SimpleUnit(val data: Unit) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(UnitSerializer(), "simpleUnit", ::SimpleUnit, SimpleUnit::data)
    }
}

data class SimpleBoolean(val data: Boolean) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Boolean.serializer(), "simpleBoolean", ::SimpleBoolean, SimpleBoolean::data)
    }
}

data class SimpleByte(val data: Byte) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Byte.serializer(), "simpleByte", ::SimpleByte, SimpleByte::data)
    }
}

data class SimpleShort(val data: Short) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Short.serializer(), "simpleShort", ::SimpleShort, SimpleShort::data)
    }
}

data class SimpleInt(val data: Int) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Int.serializer(), "simpleInt", ::SimpleInt, SimpleInt::data)
    }
}

data class SimpleNullableInt(val data: Int?) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Int.serializer().nullable, "simpleNullableInt", ::SimpleNullableInt, SimpleNullableInt::data)
    }
}

data class SimpleLong(val data: Long) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Long.serializer(), "simpleLong", ::SimpleLong, SimpleLong::data)
    }
}

data class SimpleFloat(val data: Float) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Float.serializer(), "simpleFloat", ::SimpleFloat, SimpleFloat::data)
    }
}

data class SimpleDouble(val data: Double) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Double.serializer(), "simpleDouble", ::SimpleDouble, SimpleDouble::data)
    }
}

data class SimpleChar(val data: Char) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Char.serializer(), "simpleChar", ::SimpleChar, SimpleChar::data)
    }
}

data class SimpleString(val data: String) : SimpleInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(String.serializer(), "simpleString", ::SimpleString, SimpleString::data)
    }
}

@Serializable
@SerialName("simpleClass")
data class SimpleClass(val value: String, val otherValue: String) : SimpleInterface

@Serializable
@SerialName("simpleEnum")
enum class SimpleEnum : SimpleInterface {
    TEST, TEST2;
}

fun SerialDescriptor.withName(newName: String): SerialDescriptor {
    return object : SerialDescriptor by this {
        override val serialName: String = newName
    }
}

class UnwrappedValueSerializer<S, T>(private val valueSerializer: KSerializer<S>, descriptorName: String, private val fromSource: (S) -> T, private val toSource: (T) -> S) : KSerializer<T> {
    override val descriptor: SerialDescriptor = valueSerializer.descriptor.withName(descriptorName)

    override fun deserialize(decoder: Decoder): T {
        return fromSource(valueSerializer.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: T) {
        valueSerializer.serialize(encoder, toSource(value))
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
        SimpleEnum::class with SimpleEnum.serializer()
        SimpleNullableInt::class with SimpleNullableInt.kSerializer
        SimpleClass::class with SimpleClass.serializer()
    }
}
