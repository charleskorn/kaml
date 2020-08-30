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

package com.charleskorn.kaml

/**
 * Configuration options for parsing YAML to objects and serialising objects to YAML.
 *
 * * [encodeDefaults]: set to false to not write default property values to YAML (defaults to `true`)
 * * [strictMode]: set to true to throw an exception when reading an object that has an unknown property, or false to ignore unknown properties (defaults to `true`)
 * * [extensionDefinitionPrefix]: prefix used on root-level keys (where document root is an object) to define extensions that can later be merged (defaults to `null`, which disables extensions altogether). See https://batect.charleskorn.com/config/Overview.html#anchors-aliases-extensions-and-merging for example.
 * * [polymorphismStyle]: how to read or write the type of a polymorphic object:
 *    * [PolymorphismStyle.Tag]: use a YAML tag (eg. `!<typeOfThing> { property: value }`)
 *    * [PolymorphismStyle.Property]: use a property (eg. `{ type: typeOfThing, property: value }`)
 * * [encodingIndentationSize]: number of spaces to use as indentation when encoding objects as YAML
 * * [breakScalarsAt]: maximum length of scalars when encoding objects as YAML (scalars exceeding this length will be split into multiple lines)
 */
public data class YamlConfiguration constructor(
    internal val encodeDefaults: Boolean = true,
    internal val strictMode: Boolean = true,
    internal val extensionDefinitionPrefix: String? = null,
    internal val polymorphismStyle: PolymorphismStyle = PolymorphismStyle.Tag,
    internal val encodingIndentationSize: Int = 2,
    internal val breakScalarsAt: Int = 80
)

public enum class PolymorphismStyle {
    Tag,
    Property
}
