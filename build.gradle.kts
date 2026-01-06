plugins {
    kotlin("jvm") version "2.2.21" apply false
    id("com.google.devtools.ksp") version "2.2.21-2.0.4" apply false
}

allprojects {
    group = "com.bangbang93.kdraft"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
