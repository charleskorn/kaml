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
    val kSerializer: KSerializer<SimpleNull> = Int.serializer().nullable.mapped("simpleNull", { SimpleNull }, { null })
}

data class SimpleBoolean(val data: Boolean) : SimpleInterface {
    companion object {
        val kSerializer = Boolean.serializer().mapped("simpleBoolean", ::SimpleBoolean, SimpleBoolean::data)
    }
}

data class SimpleByte(val data: Byte) : SimpleInterface {
    companion object {
        val kSerializer = Byte.serializer().mapped("simpleByte", ::SimpleByte, SimpleByte::data)
    }
}

data class SimpleShort(val data: Short) : SimpleInterface {
    companion object {
        val kSerializer = Short.serializer().mapped("simpleShort", ::SimpleShort, SimpleShort::data)
    }
}

data class SimpleInt(val data: Int) : SimpleInterface {
    companion object {
        val kSerializer = Int.serializer().mapped("simpleInt", ::SimpleInt, SimpleInt::data)
    }
}

data class SimpleNullableInt(val data: Int?) : SimpleInterface {
    companion object {
        val kSerializer = Int.serializer().nullable.mapped("simpleNullableInt", ::SimpleNullableInt, SimpleNullableInt::data)
    }
}

data class SimpleLong(val data: Long) : SimpleInterface {
    companion object {
        val kSerializer = Long.serializer().mapped("simpleLong", ::SimpleLong, SimpleLong::data)
    }
}

data class SimpleFloat(val data: Float) : SimpleInterface {
    companion object {
        val kSerializer = Float.serializer().mapped("simpleFloat", ::SimpleFloat, SimpleFloat::data)
    }
}

data class SimpleDouble(val data: Double) : SimpleInterface {
    companion object {
        val kSerializer = Double.serializer().mapped("simpleDouble", ::SimpleDouble, SimpleDouble::data)
    }
}

data class SimpleChar(val data: Char) : SimpleInterface {
    companion object {
        val kSerializer = Char.serializer().mapped("simpleChar", ::SimpleChar, SimpleChar::data)
    }
}

data class SimpleString(val data: String) : SimpleInterface {
    companion object {
        val kSerializer = String.serializer().mapped("simpleString", ::SimpleString, SimpleString::data)
    }
}

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

inline fun <S, T> KSerializer<S>.mapped(descriptorName: String = descriptor.serialName, crossinline fromSource: (S) -> T, crossinline toSource: (T) -> S): KSerializer<T> {
    return object : KSerializer<T> {
        override val descriptor: SerialDescriptor = this@mapped.descriptor.withName(descriptorName)

        override fun deserialize(decoder: Decoder): T = fromSource(this@mapped.deserialize(decoder))

        override fun serialize(encoder: Encoder, value: T) = this@mapped.serialize(encoder, toSource(value))
    }
}

@Serializable
data class SimpleWrapper(val test: SimpleInterface)

val simpleModule = SerializersModule {
    polymorphic(SimpleInterface::class) {
        SimpleNull::class with SimpleNull.kSerializer
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
    }
}
