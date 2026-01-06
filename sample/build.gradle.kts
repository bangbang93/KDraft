plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    application
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(project(":annotations"))
    ksp(project(":processor"))
}

application {
    mainClass.set("com.bangbang93.kdraft.sample.MainKt")
}
