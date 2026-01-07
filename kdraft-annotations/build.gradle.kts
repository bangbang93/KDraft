plugins {
  alias(libs.plugins.kotlin.jvm)
  `maven-publish`
  signing
}

dependencies { implementation(libs.kotlin.stdlib) }

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
        name.set("KDraft Annotations")
        description.set("Annotations for KDraft")
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

  repositories {
    maven {
      name = "localStaging"
      url = uri(layout.buildDirectory.dir("staging-deploy"))
    }
  }
}

signing {
  // 使用环境变量配置签名
  val signingKey: String? by project
  val signingPassword: String? by project
  if (signingKey != null && signingPassword != null) {
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
  }
}
