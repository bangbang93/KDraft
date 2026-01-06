package com.bangbang93.kdraft.sample

fun main() {
    // Using the generated Draft DSL
    val user = userDraft {
        id = 1
        name = "John Doe"
        email = "somebody@example.com"
        set("age", 30)
    }

    println("Created user: $user")
    println("ID: ${user.id}")
    println("Name: ${user.name}")
    println("Email: ${user.email}")
    println("Age: ${user.age}")

    // Test error handling for invalid property
    try {
        userDraft {
            set("invalidProperty", 123)
        }
    } catch (e: IllegalArgumentException) {
        println("\nError handling test: ${e.message}")
    }
}
