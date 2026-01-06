# KDraft

A Kotlin KSP (Kotlin Symbol Processing) development environment demonstration project.

## Project Information

- **Group ID**: com.bangbang93.kdraft
- **Artifact ID**: kdraft
- **Project Coordinates**: com.bangbang93.kdraft:kdraft

## Project Structure

This is a multi-module Gradle project demonstrating KSP usage:

```
KDraft/
├── annotations/        # Annotation definitions module
├── processor/         # KSP processor module
└── sample/           # Sample usage module
```

## Modules

### annotations
Contains annotation definitions that can be used to mark code for processing.
- `@GenerateBuilder`: Generates a builder pattern for data classes

### processor
Contains the KSP processor implementation that processes annotations and generates code.
- Uses KotlinPoet for code generation
- Implements SymbolProcessor to process `@GenerateBuilder` annotations

### sample
Demonstrates how to use the annotations and generated code.

## Dependencies

All dependencies are managed using Gradle Version Catalog (`gradle/libs.versions.toml`):

- **Kotlin**: 2.2.21 (Latest stable)
- **KSP**: 2.2.21-2.0.4 (Latest stable)
- **KotlinPoet**: 2.2.0 (Latest stable)
- **Gradle**: 8.10

### Version Catalog Benefits

- Centralized dependency management
- Type-safe dependency accessors
- Easy version updates across all modules
- Better IDE support with autocomplete

## Building

```bash
./gradlew build
```

## Running the Sample

```bash
./gradlew :sample:run
```

This will demonstrate the generated builder pattern for the `Person` class.

## How It Works

1. Annotate a class with `@GenerateBuilder`
2. KSP processor generates a builder class during compilation
3. Use the generated builder to construct instances

Example:
```kotlin
@GenerateBuilder
data class Person(
    var name: String = "",
    var age: Int = 0,
    var email: String = ""
)

// Generated code allows:
val person = PersonBuilder()
    .name("John Doe")
    .age(30)
    .email("john.doe@example.com")
    .build()
```

## Generated Code

The KSP processor automatically generates a builder class with:
- Fluent API methods for each property
- Type-safe property setters
- Build validation ensuring all required properties are set
- Generated code is placed in `build/generated/ksp/main/kotlin/`
