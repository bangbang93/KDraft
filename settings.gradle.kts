pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

rootProject.name = "kdraft"

include(":kdraft-annotations")
include(":kdraft-processor")
include(":kdraft-sample")
