/*

   Copyright 2018 Charles Korn.

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

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
open class Benchmarks {
    private val singleItemList = generateList(1)
    private val twoItemList = generateList(2)
    private val tenItemList = generateList(10)
    private val twentyItemList = generateList(20)
    private val fiftyItemList = generateList(50)
    private val hundredItemList = generateList(100)

    private val singleItemMap = generateMap(1)
    private val twoItemMap = generateMap(2)
    private val tenItemMap = generateMap(10)
    private val twentyItemMap = generateMap(20)
    private val fiftyItemMap = generateMap(50)
    private val hundredItemMap = generateMap(100)

    @Benchmark
    fun parseSingleItemList(): YamlNode = parse(singleItemList)

    @Benchmark
    fun parseTwoItemList(): YamlNode = parse(twoItemList)

    @Benchmark
    fun parseTenItemList(): YamlNode = parse(tenItemList)

    @Benchmark
    fun parseTwentyItemList(): YamlNode = parse(twentyItemList)

    @Benchmark
    fun parseFiftyItemList(): YamlNode = parse(fiftyItemList)

    @Benchmark
    fun parseHundredItemList(): YamlNode = parse(hundredItemList)

    @Benchmark
    fun parseSingleItemMap(): YamlNode = parse(singleItemMap)

    @Benchmark
    fun parseTwoItemMap(): YamlNode = parse(twoItemMap)

    @Benchmark
    fun parseTenItemMap(): YamlNode = parse(tenItemMap)

    @Benchmark
    fun parseTwentyItemMap(): YamlNode = parse(twentyItemMap)

    @Benchmark
    fun parseFiftyItemMap(): YamlNode = parse(fiftyItemMap)

    @Benchmark
    fun parseHundredItemMap(): YamlNode = parse(hundredItemMap)

    private fun generateList(n: Int) = (1..n).joinToString("\n") { i -> "- thing$i" }
    private fun generateMap(n: Int) = (1..n).joinToString("\n") { i -> "thing$i: value$i" }

    private fun parse(input: String): YamlNode {
        val parser = YamlParser(input)

        return YamlNode.fromParser(parser)
    }
}
