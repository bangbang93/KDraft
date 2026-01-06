plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":annotations"))
    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("com.squareup:kotlinpoet-ksp:2.2.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.2.21-2.0.4")
}
