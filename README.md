# KDraft

A Kotlin KSP (Kotlin Symbol Processing) annotation processor for automatic Draft DSL Builder code generation.

## Project Information

- **Group ID**: com.bangbang93.kdraft
- **Artifact ID**: kdraft
- **Project Coordinates**: com.bangbang93.kdraft:kdraft

## Features

- **@Draftable Annotation**: Mark data classes to automatically generate Draft builders
- **DSL-style Builders**: Fluent, type-safe builder pattern using Kotlin DSL syntax
- **Dynamic Property Setting**: Set properties by name using the `set(propertyName, value)` method
- **Type Safety**: Generated code maintains full type safety

## Project Structure

```
kdraft/
├── kdraft-annotations/    # Annotation definitions module
├── kdraft-processor/      # KSP processor module
└── kdraft-sample/         # Sample usage module
```

## Dependencies

All dependencies are managed using Gradle Version Catalog (`gradle/libs.versions.toml`):

- **Kotlin**: 2.3.0
- **KSP**: 2.3.4
- **KotlinPoet**: 2.2.0
- **Gradle**: 9.0

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

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
```

### 2. Annotate Your Data Class

```kotlin
import com.bangbang93.kdraft.Draftable

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

## Publishing

This project uses **maven-publish** plugin to publish to Maven Central Portal.

### Maven Central (新版本)

To publish to Maven Central Portal (central.sonatype.com):

**Quick setup:**

```bash
# 1. 注册并验证 namespace
https://central.sonatype.com/

# 2. 配置凭证（~/.gradle/gradle.properties）
centralUsername=YOUR_TOKEN_USERNAME
centralPassword=YOUR_TOKEN_PASSWORD
signing.keyId=YOUR_GPG_KEY_ID
signing.password=YOUR_GPG_PASSPHRASE
signing.secretKeyRingFile=/home/bangbang93/.gnupg/secring.gpg

# 3. 发布
./gradlew publish
```

**详细指南：** [MAVEN-CENTRAL-PORTAL.md](./MAVEN-CENTRAL-PORTAL.md)

### Local Testing

```bash
./gradlew publishToMavenLocal
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
