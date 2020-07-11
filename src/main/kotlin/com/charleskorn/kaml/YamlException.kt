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

import org.snakeyaml.engine.v2.events.Event

open class YamlException(
    override val message: String,
    val line: Int,
    val column: Int,
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {
    constructor(message: String, location: Location, cause: Throwable? = null) : this(message, location.line, location.column, cause)

    val location: Location = Location(line, column)
}

class DuplicateKeyException(val originalLocation: Location, val duplicateLocation: Location, val key: String) :
    YamlException("Duplicate key $key. It was previously given at line ${originalLocation.line}, column ${originalLocation.column}.", duplicateLocation)

class EmptyYamlDocumentException(message: String, location: Location) : YamlException(message, location)

class InvalidPropertyValueException(val propertyName: String, val reason: String, location: Location, cause: Throwable?) : YamlException("Value for '$propertyName' is invalid: $reason", location, cause)

class MalformedYamlException(message: String, location: Location) : YamlException(message, location)

class UnexpectedNullValueException(location: Location) : YamlException("Unexpected null or empty value for non-null field.", location)

class UnknownPropertyException(val propertyName: String, val validPropertyNames: Set<String>, location: Location) :
    YamlException("Unknown property '$propertyName'. Known properties are: ${validPropertyNames.sorted().joinToString(", ")}", location)

class UnknownPolymorphicTypeException(val typeName: String, val validTypeNames: Set<String>, location: Location, cause: Throwable? = null) :
    YamlException("Unknown type '$typeName'. Known types are: ${validTypeNames.sorted().joinToString(", ")}", location, cause)

class YamlScalarFormatException(message: String, location: Location, val originalValue: String) : YamlException(message, location)

open class IncorrectTypeException(message: String, location: Location) : YamlException(message, location)

class MissingTypeTagException(location: Location) : IncorrectTypeException("Value is missing a type tag (eg. !<type>)", location)

class UnknownAnchorException(val anchorName: String, location: Location) :
    YamlException("Unknown anchor '$anchorName'.", location)

class NoAnchorForExtensionException(val key: String, val extensionDefinitionPrefix: String, location: Location) :
    YamlException("The key $key starts with the extension definition prefix '$extensionDefinitionPrefix' but does not define an anchor.", location)

val Event.location: Location
    get() = Location(startMark.get().line + 1, startMark.get().column + 1)
