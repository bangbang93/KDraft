package com.bangbang93.kdraft

/**
 * Annotation to mark data classes for automatic Draft DSL Builder generation.
 * 
 * When applied to a data class, the KSP processor will generate:
 * - A Draft class with mutable properties for building
 * - A DSL function for convenient instance construction
 * - A dynamic set() method for property assignment by name
 * 
 * Example usage:
 * ```
 * @Draftable
 * data class User(
 *     val id: Int,
 *     val name: String,
 *     val email: String,
 *     val age: Int
 * )
 * 
 * // Generated code allows:
 * val user = userDraft {
 *     id = 1
 *     name = "John Doe"
 *     email = "somebody@example.com"
 *     set("age", 30)
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Draftable
