# kaml

[![Build Status](https://img.shields.io/travis/com/charleskorn/kaml/master.svg)](https://travis-ci.com/charleskorn/kaml)
[![Coverage](https://img.shields.io/codecov/c/github/charleskorn/kaml.svg)](https://codecov.io/gh/charleskorn/kaml)
[![License](https://img.shields.io/github/license/charleskorn/kaml.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.charleskorn.kaml/kaml.svg?label=maven%20central)](https://search.maven.org/search?q=g:%22com.charleskorn.kaml%22%20AND%20a:%22kaml%22)

## What is this?

This library adds YAML support to [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/).

YAML version 1.2 is supported.

## Usage samples

### Parsing from YAML to a Kotlin object

```kotlin
@Serializable
data class Team(
    val leader: String,
    val members: List<String>
)

val input = """
        leader: Amy
        members:
          - Bob
          - Cindy
          - Dan
    """.trimIndent()

val result = Yaml.default.decodeFromString(Team.serializer(), input)

println(result)
```

### Serializing from a Kotlin object to YAML

```kotlin
@Serializable
data class Team(
    val leader: String,
    val members: List<String>
)

val input = Team("Amy", listOf("Bob", "Cindy", "Dan"))

val result = Yaml.default.encodeToString(Team.serializer(), input)

println(result)
```

## Features

* Supports most major YAML features:
  * Scalars, including strings, booleans, integers and floats
  * [Sequences (lists)](https://yaml.org/type/seq.html)
  * [Maps](https://yaml.org/type/map.html)
  * [Nulls](https://yaml.org/type/null.html)
  * [Aliases and anchors](https://yaml.org/spec/1.2/spec.html#id2765878), including [merging aliases to form one map](https://yaml.org/type/merge.html)

* Supports parsing YAML to Kotlin objects (deserializing) and writing Kotlin objects as YAML (serializing)

* Supports [kotlinx.serialization's polymorphism](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md) for sealed and unsealed types

  Two styles are available (set `YamlConfiguration.polymorphismStyle` when creating an instance of `Yaml`):

  * using [YAML tags](https://yaml.org/spec/1.2/spec.html#id2761292) to specify the type:

    ```yaml
    servers:
      - !<frontend>
        hostname: a.mycompany.com
      - !<backend>
        database: db-1
    ```

  * using a `type` property to specify the type:

    ```yaml
    servers:
      - type: frontend
        hostname: a.mycompany.com
      - type: backend
        database: db-1
    ```

  The fragments above could be generated with:

  ```kotlin
  @Serializable
  sealed class Server {
    @SerialName("frontend")
    @Serializable
    data class Frontend(val hostname: String)

    @SerialName("backend")
    @Serializable
    data class Backend(val database: String)
  }

  @Serializable
  data class Config(val servers: List<Server>)

  val config = Config(listOf(
    Frontend("a.mycompany.com"),
    Backend("db-1")
  ))

  val result = Yaml.default.encodeToString(Config.serializer(), config)

  println(result)
  ```

* Supports [Docker Compose-style extension fields](https://medium.com/@kinghuang/docker-compose-anchors-aliases-extensions-a1e4105d70bd)

  ```yaml
  x-common-labels: &common-labels
    labels:
      owned-by: myteam@mycompany.com
      cost-centre: myteam

  servers:
    server-a:
      <<: *common-labels
      kind: frontend

    server-b:
      <<: *common-labels
      kind: backend

    # server-b and server-c are equivalent
    server-c:
      labels:
        owned-by: myteam@mycompany.com
        cost-centre: myteam
      kind: backend
  ```

  Specify the extension prefix by setting `YamlConfiguration.extensionDefinitionPrefix` when creating an instance of `Yaml` (eg. `"x-"` for the example above).

  Extensions can only be defined at the top level of a document, and only if the top level element is a map or object. Any key starting with the extension prefix must have an anchor defined (`&...`) and will not be included in the deserialised value.

## Referencing kaml

Add the following to your Gradle build script:

```kotlin
implementation("com.charleskorn.kaml:kaml:<version number here>")
```

Check the [releases page](https://github.com/charleskorn/kaml/releases) for the latest release information,
and the [Maven Central page](https://search.maven.org/artifact/com.charleskorn.kaml/kaml) for examples of how
to reference the library in other build systems.

## Contributing to kaml

Pull requests and bug reports are always welcome!

kaml uses [batect](https://batect.charleskorn.com) to simplify development environment setup:

* To build the library: `./batect build`
* To run the tests and static analysis tools: `./batect check`
* To run the tests and static analysis tools continuously: `./batect continuousCheck`

Other commands are available by running `./batect --list-tasks`

## Reference links

* [YAML 1.2 Specification](http://yaml.org/spec/1.2/spec.html)
* [snakeyaml-engine](https://bitbucket.org/asomov/snakeyaml-engine), the YAML parser this library is based on
