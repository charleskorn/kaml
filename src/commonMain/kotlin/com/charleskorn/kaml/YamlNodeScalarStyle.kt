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

import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle

public enum class YamlNodeScalarStyle {
    DOUBLE_QUOTED,
    SINGLE_QUOTED,
    LITERAL,
    FOLDED,
    JSON_SCALAR_STYLE,
    PLAIN,
    ;

    public fun toScalarStyle(): ScalarStyle = when (this) {
        DOUBLE_QUOTED -> ScalarStyle.DOUBLE_QUOTED
        SINGLE_QUOTED -> ScalarStyle.SINGLE_QUOTED
        LITERAL -> ScalarStyle.LITERAL
        FOLDED -> ScalarStyle.FOLDED
        JSON_SCALAR_STYLE -> ScalarStyle.JSON_SCALAR_STYLE
        PLAIN -> ScalarStyle.PLAIN
    }

    public companion object {
        public fun fromScalarStyle(scalarStyle: ScalarStyle): YamlNodeScalarStyle = when (scalarStyle) {
            ScalarStyle.DOUBLE_QUOTED -> DOUBLE_QUOTED
            ScalarStyle.SINGLE_QUOTED -> SINGLE_QUOTED
            ScalarStyle.LITERAL -> LITERAL
            ScalarStyle.FOLDED -> FOLDED
            ScalarStyle.JSON_SCALAR_STYLE -> JSON_SCALAR_STYLE
            ScalarStyle.PLAIN -> PLAIN
        }
    }
}
