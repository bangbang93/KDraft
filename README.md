# KDraft

[![Maven Central](https://img.shields.io/maven-central/v/com.bangbang93.kdraft/kdraft-annotations?label=Maven%20Central)](https://central.sonatype.com/namespace/com.bangbang93.kdraft)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![KSP](https://img.shields.io/badge/KSP-2.3.4-blue.svg)](https://github.com/google/ksp)
[![GitHub release](https://img.shields.io/github/v/release/bangbang93/KDraft)](https://github.com/bangbang93/KDraft/releases)

A Kotlin KSP (Kotlin Symbol Processing) annotation processor for automatic Draft DSL Builder code generation.

## Features

- **@Draftable Annotation**: Mark data classes to automatically generate Draft builders
- **DSL-style Builders**: Fluent, type-safe builder pattern using Kotlin DSL syntax
- **Dynamic Property Setting**: Set properties by name using the `set(propertyName, value)` method
- **Type Safety**: Generated code maintains full type safety

## Usage

### 1. Add Dependencies

In your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "2.3.0"
    id("com.google.devtools.ksp") version "2.3.4"
}

dependencies {
    implementation("com.bangbang93.kdraft:kdraft-annotations:1.0.0")
    ksp("com.bangbang93.kdraft:kdraft-processor:1.0.0")
}

```

### 2. Annotate Your Data Class

```kotlin
@Draftable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int
)
```

### 3. Use the Generated DSL

```kotlin
val user = userDraft {
    id = 1
    name = "John Doe"
    email = "somebody@example.com"
    set("age", 30)
}
```

## Generated Code

For the `User` class above, the processor generates:

```kotlin
class UserDraft {
    var id: Int = 0
    var name: String = ""
    var email: String = ""
    var age: Int = 0

    fun set(propertyName: String, value: Any?) {
        when (propertyName) {
            "id" -> id = value as Int
            "name" -> name = value as String
            "email" -> email = value as String
            "age" -> age = value as Int
            else -> throw IllegalArgumentException("Unknown property: $propertyName")
        }
    }

    fun build(): User = User(id, name, email, age)
}

fun userDraft(block: UserDraft.() -> Unit): User {
    return UserDraft().apply(block).build()
}
```

## Building

```bash
./gradlew build
```

## Running the Sample

```bash
./gradlew :kdraft-sample:run
```

## How It Works

1. Annotate a data class with `@Draftable`
2. KSP processor scans for annotated classes during compilation
3. For each annotated class, it generates:
    - A Draft class with mutable properties
    - A DSL function for convenient instance construction
    - A `set()` method for dynamic property assignment
4. Generated code is placed in `build/generated/ksp/main/kotlin/`

## Type Support

The processor supports the following types with default values:

| Type           | Default Value |
|----------------|---------------|
| Int            | 0             |
| Long           | 0L            |
| Short          | 0             |
| Byte           | 0             |
| Float          | 0.0f          |
| Double         | 0.0           |
| Boolean        | false         |
| Char           | '\u0000'      |
| String         | ""            |
| List           | emptyList()   |
| Set            | emptySet()    |
| Map            | emptyMap()    |
| Nullable types | null          |

## License

MIT License
