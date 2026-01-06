plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    application
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":annotations"))
    ksp(project(":processor"))
}

application {
    mainClass.set("com.bangbang93.kdraft.sample.MainKt")
}
