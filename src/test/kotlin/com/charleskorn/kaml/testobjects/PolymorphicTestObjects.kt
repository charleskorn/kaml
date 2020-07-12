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

interface PolymorphicInterface

object PolymorphicNull : PolymorphicInterface {
    val kSerializer: KSerializer<PolymorphicNull> = UnwrappedValueSerializer(UnitSerializer().nullable, "simpleNull", { PolymorphicNull }, { null })
}

data class PolymorphicUnit(val data: Unit) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(UnitSerializer(), "simpleUnit", ::PolymorphicUnit, PolymorphicUnit::data)
    }
}

data class PolymorphicBoolean(val data: Boolean) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Boolean.serializer(), "simpleBoolean", ::PolymorphicBoolean, PolymorphicBoolean::data)
    }
}

data class PolymorphicByte(val data: Byte) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Byte.serializer(), "simpleByte", ::PolymorphicByte, PolymorphicByte::data)
    }
}

data class PolymorphicShort(val data: Short) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Short.serializer(), "simpleShort", ::PolymorphicShort, PolymorphicShort::data)
    }
}

data class PolymorphicInt(val data: Int) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Int.serializer(), "simpleInt", ::PolymorphicInt, PolymorphicInt::data)
    }
}

data class PolymorphicNullableInt(val data: Int?) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Int.serializer().nullable, "simpleNullableInt", ::PolymorphicNullableInt, PolymorphicNullableInt::data)
    }
}

data class PolymorphicLong(val data: Long) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Long.serializer(), "simpleLong", ::PolymorphicLong, PolymorphicLong::data)
    }
}

data class PolymorphicFloat(val data: Float) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Float.serializer(), "simpleFloat", ::PolymorphicFloat, PolymorphicFloat::data)
    }
}

data class PolymorphicDouble(val data: Double) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Double.serializer(), "simpleDouble", ::PolymorphicDouble, PolymorphicDouble::data)
    }
}

data class PolymorphicChar(val data: Char) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Char.serializer(), "simpleChar", ::PolymorphicChar, PolymorphicChar::data)
    }
}

data class PolymorphicString(val data: String) : PolymorphicInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(String.serializer(), "simpleString", ::PolymorphicString, PolymorphicString::data)
    }
}

@Serializable
@SerialName("simpleClass")
data class PolymorphicClass(val value: String, val otherValue: String) : PolymorphicInterface

@Serializable
@SerialName("simpleEnum")
enum class PolymorphicEnum : PolymorphicInterface {
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
data class PolymorphicWrapper(val test: PolymorphicInterface)

val polymorphicModule = SerializersModule {
    polymorphic(PolymorphicInterface::class) {
        PolymorphicNull::class with PolymorphicNull.kSerializer
        PolymorphicUnit::class with PolymorphicUnit.kSerializer
        PolymorphicBoolean::class with PolymorphicBoolean.kSerializer
        PolymorphicByte::class with PolymorphicByte.kSerializer
        PolymorphicShort::class with PolymorphicShort.kSerializer
        PolymorphicInt::class with PolymorphicInt.kSerializer
        PolymorphicLong::class with PolymorphicLong.kSerializer
        PolymorphicFloat::class with PolymorphicFloat.kSerializer
        PolymorphicDouble::class with PolymorphicDouble.kSerializer
        PolymorphicChar::class with PolymorphicChar.kSerializer
        PolymorphicString::class with PolymorphicString.kSerializer
        PolymorphicEnum::class with PolymorphicEnum.serializer()
        PolymorphicNullableInt::class with PolymorphicNullableInt.kSerializer
        PolymorphicClass::class with PolymorphicClass.serializer()
    }
}
