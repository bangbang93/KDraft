plugins {
  base
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.jreleaser)
}

allprojects {
  apply(plugin = "base")

  group = "com.bangbang93.kdraft"
  version = "0.0.1"

  repositories { mavenCentral() }
}
