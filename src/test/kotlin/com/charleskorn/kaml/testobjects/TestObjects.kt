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

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

@Serializable
data class SimpleStructure(
    val name: String
)

@Serializable
data class Team(
    val members: List<String>
)

@Serializable
data class NestedObjects(
    val firstPerson: SimpleStructure,
    val secondPerson: SimpleStructure
)

enum class TestEnum {
    Value1,
    Value2
}

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
