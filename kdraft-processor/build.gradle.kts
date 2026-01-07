plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  `maven-publish`
}

dependencies {
  implementation(libs.kotlin.stdlib)
  implementation(project(":kdraft-annotations"))
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)
  implementation(libs.ksp.api)
}

// 配置 Java 插件以支持源码和文档 JAR
java {
  withSourcesJar()
  withJavadocJar()
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      pom {
        name.set("KDraft Processor")
        description.set("KSP processor for KDraft")
        url.set("https://github.com/bangbang93/KDraft")
        inceptionYear.set("2026")

        licenses {
          license {
            name.set("MIT License")
            url.set("https://opensource.org/licenses/MIT")
          }
        }

        developers {
          developer {
            id.set("bangbang93")
            name.set("bangbang93")
          }
        }

        scm {
          connection.set("scm:git:git://github.com/bangbang93/KDraft.git")
          developerConnection.set("scm:git:ssh://git@github.com:bangbang93/KDraft.git")
          url.set("https://github.com/bangbang93/KDraft")
        }
      }
    }
  }
}
