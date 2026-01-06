plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    group = "com.bangbang93.kdraft"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
