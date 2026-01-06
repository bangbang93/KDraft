package com.bangbang93.kdraft.sample

fun main() {
    // This will use the generated PersonBuilder
    val person = PersonBuilder()
        .name("John Doe")
        .age(30)
        .email("john.doe@example.com")
        .build()

    println("Created person: $person")
    println("Name: ${person.name}")
    println("Age: ${person.age}")
    println("Email: ${person.email}")
}
