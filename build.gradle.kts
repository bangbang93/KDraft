plugins {
  base
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.publish)
}

allprojects {
  apply(plugin = "base")

  group = "com.bangbang93.kdraft"
  version = "0.0.1"

  repositories { mavenCentral() }
}

nexusPublishing {
  repositories {
    // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
    sonatype {
      nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
      snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
    }
  }
}
