/*

   Copyright 2018-2020 Charles Korn.

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

public open class YamlException(
    override val message: String,
    public val line: Int,
    public val column: Int,
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {
    public constructor(message: String, location: Location, cause: Throwable? = null) : this(message, location.line, location.column, cause)

    public val location: Location = Location(line, column)

    override fun toString(): String = "${this::class.qualifiedName} at line $line, column $column: $message"
}

public class DuplicateKeyException(
    public val originalLocation: Location,
    public val duplicateLocation: Location,
    public val key: String
) :
    YamlException("Duplicate key $key. It was previously given at line ${originalLocation.line}, column ${originalLocation.column}.", duplicateLocation)

public class EmptyYamlDocumentException(message: String, location: Location) : YamlException(message, location)

public class InvalidPropertyValueException(
    public val propertyName: String,
    public val reason: String,
    location: Location,
    cause: Throwable? = null
) : YamlException("Value for '$propertyName' is invalid: $reason", location, cause)

public class MalformedYamlException(message: String, location: Location) : YamlException(message, location)

public class UnexpectedNullValueException(location: Location) : YamlException("Unexpected null or empty value for non-null field.", location)

public class MissingRequiredPropertyException(
    public val propertyName: String,
    location: Location,
    cause: Throwable? = null
) :
    YamlException("Property '$propertyName' is required but it is missing.", location, cause)

public class UnknownPropertyException(
    public val propertyName: String,
    public val validPropertyNames: Set<String>,
    location: Location
) :
    YamlException("Unknown property '$propertyName'. Known properties are: ${validPropertyNames.sorted().joinToString(", ")}", location)

public class UnknownPolymorphicTypeException(
    public val typeName: String,
    public val validTypeNames: Set<String>,
    location: Location,
    cause: Throwable? = null
) :
    YamlException("Unknown type '$typeName'. Known types are: ${validTypeNames.sorted().joinToString(", ")}", location, cause)

public class YamlScalarFormatException(
    message: String,
    location: Location,
    public val originalValue: String
) : YamlException(message, location)

public open class IncorrectTypeException(message: String, location: Location) : YamlException(message, location)

public class MissingTypeTagException(location: Location) :
    IncorrectTypeException("Value is missing a type tag (eg. !<type>)", location)

public class UnknownAnchorException(public val anchorName: String, location: Location) :
    YamlException("Unknown anchor '$anchorName'.", location)

public class NoAnchorForExtensionException(
    public val key: String,
    public val extensionDefinitionPrefix: String,
    location: Location
) :
    YamlException("The key '$key' starts with the extension definition prefix '$extensionDefinitionPrefix' but does not define an anchor.", location)

internal val Event.location: Location
    get() = Location(startMark.get().line + 1, startMark.get().column + 1)
