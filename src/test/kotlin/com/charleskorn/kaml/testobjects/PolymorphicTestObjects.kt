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

open class UnsealedClass

@Serializable
@SerialName("unsealedBoolean")
data class UnsealedBoolean(val value: Boolean) : UnsealedClass()

@Serializable
@SerialName("unsealedString")
data class UnsealedString(val value: String) : UnsealedClass()

interface UnwrappedInterface

object UnwrappedNull : UnwrappedInterface {
    val kSerializer: KSerializer<UnwrappedNull> = UnwrappedValueSerializer(UnitSerializer().nullable, "simpleNull", { UnwrappedNull }, { null })
}

data class UnwrappedUnit(val data: Unit) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(UnitSerializer(), "simpleUnit", ::UnwrappedUnit, UnwrappedUnit::data)
    }
}

data class UnwrappedBoolean(val data: Boolean) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Boolean.serializer(), "simpleBoolean", ::UnwrappedBoolean, UnwrappedBoolean::data)
    }
}

data class UnwrappedByte(val data: Byte) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Byte.serializer(), "simpleByte", ::UnwrappedByte, UnwrappedByte::data)
    }
}

data class UnwrappedShort(val data: Short) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Short.serializer(), "simpleShort", ::UnwrappedShort, UnwrappedShort::data)
    }
}

data class UnwrappedInt(val data: Int) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Int.serializer(), "simpleInt", ::UnwrappedInt, UnwrappedInt::data)
    }
}

data class UnwrappedNullableInt(val data: Int?) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Int.serializer().nullable, "simpleNullableInt", ::UnwrappedNullableInt, UnwrappedNullableInt::data)
    }
}

data class UnwrappedLong(val data: Long) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Long.serializer(), "simpleLong", ::UnwrappedLong, UnwrappedLong::data)
    }
}

data class UnwrappedFloat(val data: Float) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Float.serializer(), "simpleFloat", ::UnwrappedFloat, UnwrappedFloat::data)
    }
}

data class UnwrappedDouble(val data: Double) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Double.serializer(), "simpleDouble", ::UnwrappedDouble, UnwrappedDouble::data)
    }
}

data class UnwrappedChar(val data: Char) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(Char.serializer(), "simpleChar", ::UnwrappedChar, UnwrappedChar::data)
    }
}

data class UnwrappedString(val data: String) : UnwrappedInterface {
    companion object {
        val kSerializer = UnwrappedValueSerializer(String.serializer(), "simpleString", ::UnwrappedString, UnwrappedString::data)
    }
}

@Serializable
@SerialName("simpleClass")
data class UnwrappedClass(val value: String, val otherValue: String) : UnwrappedInterface

@Serializable
@SerialName("simpleEnum")
enum class UnwrappedEnum : UnwrappedInterface {
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
data class PolymorphicWrapper(val test: UnwrappedInterface)

val polymorphicModule = SerializersModule {
    polymorphic(UnwrappedInterface::class) {
        UnwrappedNull::class with UnwrappedNull.kSerializer
        UnwrappedUnit::class with UnwrappedUnit.kSerializer
        UnwrappedBoolean::class with UnwrappedBoolean.kSerializer
        UnwrappedByte::class with UnwrappedByte.kSerializer
        UnwrappedShort::class with UnwrappedShort.kSerializer
        UnwrappedInt::class with UnwrappedInt.kSerializer
        UnwrappedLong::class with UnwrappedLong.kSerializer
        UnwrappedFloat::class with UnwrappedFloat.kSerializer
        UnwrappedDouble::class with UnwrappedDouble.kSerializer
        UnwrappedChar::class with UnwrappedChar.kSerializer
        UnwrappedString::class with UnwrappedString.kSerializer
        UnwrappedEnum::class with UnwrappedEnum.serializer()
        UnwrappedNullableInt::class with UnwrappedNullableInt.kSerializer
        UnwrappedClass::class with UnwrappedClass.serializer()
    }

    polymorphic(UnsealedClass::class) {
        UnsealedBoolean::class with UnsealedBoolean.serializer()
        UnsealedString::class with UnsealedString.serializer()
    }
}
