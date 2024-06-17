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

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.KotestTestScope
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.spec.style.scopes.RootTestWithConfigBuilder
import io.kotest.core.test.TestScope
import kotlin.jvm.JvmName

private fun String.toContainerPrefix(): String = "$this -- "

/**
 * Flat version of a Kotest FunSpec for common and JS tests.
 *
 * WORKAROUND https://github.com/kotest/kotest/issues/3141 (Support for nested tests on JS target)
 *
 * Kotest 5 doesn't support nested tests in Kotlin/JS, so until it is supported
 * this spec can be used, flattening all the nested tests to the root level.
 * Limitation: context() bodies are non-suspendable, only test() bodies accept suspend functions.
 *
 * Posted by @BenWoodworth in Kotest issue #3141:
 * https://github.com/kotest/kotest/issues/3141#issuecomment-1278433891
 */
// Using `abstract` causes a compiler error with native, so using `open` instead
open class FlatFunSpec private constructor() : FunSpec() {
    // Using primary constructor causes issue with Kotlin/JS IR:
    // https://youtrack.jetbrains.com/issue/KT-54450
    constructor(body: FlatFunSpec.() -> Unit = {}) : this() {
        body()
    }

    // Overload context functions with non-nested versions
    @JvmName("context\$FlatSpec")
    fun context(name: String, test: FlatSpecContainerScope.() -> Unit): Unit =
        test(FlatSpecContainerScope(this, name.toContainerPrefix(), false))

    @JvmName("xcontext\$FlatSpec")
    fun xcontext(name: String, test: FlatSpecContainerScope.() -> Unit): Unit =
        test(FlatSpecContainerScope(this, name.toContainerPrefix(), true))

    // Suppress FunSpec's context functions, so they can't be used
    @Deprecated("Unsupported", level = DeprecationLevel.HIDDEN)
    override fun context(name: String, test: suspend FunSpecContainerScope.() -> Unit): Nothing = error("Unsupported")

    @Deprecated("Unsupported", level = DeprecationLevel.HIDDEN)
    override fun xcontext(name: String, test: suspend FunSpecContainerScope.() -> Unit): Nothing = error("Unsupported")

    @ExperimentalKotest
    @Deprecated("Unsupported", level = DeprecationLevel.HIDDEN)
    override fun context(name: String): Nothing = error("Unsupported")

    @ExperimentalKotest
    @Deprecated("Unsupported", level = DeprecationLevel.HIDDEN)
    override fun xcontext(name: String): Nothing = error("Unsupported")
}

@Suppress("unused")
@KotestTestScope
class FlatSpecContainerScope(
    private val spec: FlatFunSpec,
    private val prefix: String,
    private val ignored: Boolean,
) {
    fun test(name: String): RootTestWithConfigBuilder = if (ignored) {
        spec.xtest(prefix + name)
    } else {
        spec.test(prefix + name)
    }

    fun test(name: String, test: suspend TestScope.() -> Unit): Unit = if (ignored) {
        spec.xtest(prefix + name, test)
    } else {
        spec.test(prefix + name, test)
    }

    fun xtest(name: String): RootTestWithConfigBuilder = spec.xtest(prefix + name)

    fun xtest(name: String, test: suspend TestScope.() -> Unit): Unit = spec.xtest(prefix + name, test)

    fun context(name: String, test: FlatSpecContainerScope.() -> Unit): Unit = if (ignored) {
        spec.xcontext(prefix + name, test)
    } else {
        spec.context(prefix + name, test)
    }

    fun xcontext(name: String, test: FlatSpecContainerScope.() -> Unit): Unit = spec.xcontext(prefix + name, test)
}
