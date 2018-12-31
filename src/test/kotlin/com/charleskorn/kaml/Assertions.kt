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

import ch.tutteli.atrium.api.cc.en_GB.property
import ch.tutteli.atrium.creating.Assert

fun Assert<YamlException>.line(assertionCreator: Assert<Int>.() -> Unit) {
    property(subject::line).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<YamlException>.column(assertionCreator: Assert<Int>.() -> Unit) {
    property(subject::column).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<DuplicateKeyException>.originalLocation(assertionCreator: Assert<Location>.() -> Unit) {
    property(subject::originalLocation).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<DuplicateKeyException>.duplicateLocation(assertionCreator: Assert<Location>.() -> Unit) {
    property(subject::duplicateLocation).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<DuplicateKeyException>.key(assertionCreator: Assert<String>.() -> Unit) {
    property(subject::key).addAssertionsCreatedBy(assertionCreator)
}

@JvmName("InvalidPropertyValueExceptionPropertyName")
fun Assert<InvalidPropertyValueException>.propertyName(assertionCreator: Assert<String>.() -> Unit) {
    property(subject::propertyName).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<InvalidPropertyValueException>.reason(assertionCreator: Assert<String>.() -> Unit) {
    property(subject::reason).addAssertionsCreatedBy(assertionCreator)
}

@JvmName("UnknownPropertyExceptionPropertyName")
fun Assert<UnknownPropertyException>.propertyName(assertionCreator: Assert<String>.() -> Unit) {
    property(subject::propertyName).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<UnknownPropertyException>.validPropertyNames(assertionCreator: Assert<Set<String>>.() -> Unit) {
    property(subject::validPropertyNames).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<UnsupportedYamlFeatureException>.featureName(assertionCreator: Assert<String>.() -> Unit) {
    property(subject::featureName).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<YamlScalarFormatException>.originalValue(assertionCreator: Assert<String>.() -> Unit) {
    property(subject::originalValue).addAssertionsCreatedBy(assertionCreator)
}
