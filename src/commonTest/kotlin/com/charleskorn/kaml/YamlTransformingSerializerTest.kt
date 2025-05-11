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

import com.charleskorn.kaml.testobjects.Shape
import com.charleskorn.kaml.testobjects.Shapes
import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.UppercasedValueSimpleStructure
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class YamlTransformingSerializerTest : FlatFunSpec({
    val yaml = Yaml(
        configuration = YamlConfiguration(
            polymorphismStyle = PolymorphismStyle.None,
        ),
    )

    context("no-op default implementation") {
        val objToSerialize = SimpleStructure(
            name = "kAml",
        )

        test("serialize with no transformation") {
            val output = yaml.encodeToString(objToSerialize)
            output shouldBe "name: \"kAml\""
        }

        test("deserialize with no transformation") {
            val inputYaml = "name: \"kaml\""
            val result = yaml.decodeFromString<SimpleStructure>(inputYaml)
            result.name shouldBe "kaml"
        }
    }

    context("simple value transformer") {
        val objToSerialize = UppercasedValueSimpleStructure(
            name = "kAml",
        )

        test("serialize should uppercase the string in YAML") {
            val output = yaml.encodeToString(objToSerialize)
            output shouldBe "name: \"KAML\""
        }

        test("deserialize should return uppercased string") {
            val inputYaml = "name: \"kaml\""
            val result = yaml.decodeFromString<UppercasedValueSimpleStructure>(inputYaml)
            result.name shouldBe "KAML"
        }
    }

    context("structure transformer") {
        val shapes = Shapes(
            first = Shape.Rectangle(0, 1),
            rest =
            listOf(
                Shape.Circle(1),
                Shape.Circle(2),
                Shape.Rectangle(1, 1),
            ),
        )

        test("serialize should wrap all shapes into list") {
            val output = yaml.encodeToString(shapes)
            val expectedYaml =
                """- "a": 0
  "b": 1
- "diameter": 1
- "diameter": 2
- "a": 1
  "b": 1
                """.trimIndent()
            output shouldBe expectedYaml
        }
    }
})
