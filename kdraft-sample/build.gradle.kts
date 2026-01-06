plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    application
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(project(":kdraft-annotations"))
    ksp(project(":kdraft-processor"))
}

application {
    mainClass.set("com.bangbang93.kdraft.sample.MainKt")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
