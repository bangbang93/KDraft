# JReleaser 迁移指南

本文档描述了如何从 Gradle Nexus Publish Plugin 迁移到 JReleaser。

## 迁移概述

### 迁移前
- **发布插件**: `io.github.gradle-nexus.publish-plugin` v2.0.0
- **发布目标**: Maven Central (Sonatype OSSRH)
- **发布命令**: `./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository`

### 迁移后
- **发布插件**: `org.jreleaser` v1.15.0
- **发布目标**: Maven Central (Sonatype Central Portal)
- **发布命令**: `./gradlew jreleaserFullRelease`

## 主要变更

### 1. 版本目录 (gradle/libs.versions.toml)

**迁移前:**
```toml
[versions]
nexus-publish = "2.0.0"

[plugins]
publish = { id = "io.github.gradle-nexus.publish-plugin", version.ref = "nexus-publish" }
```

**迁移后:**
```toml
[versions]
jreleaser = "1.15.0"

[plugins]
jreleaser = { id = "org.jreleaser", version.ref = "jreleaser" }
```

### 2. 根构建文件 (build.gradle.kts)

**迁移前:**
```kotlin
plugins {
  base
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.publish)
}

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
      snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
    }
  }
}
```

**迁移后:**
```kotlin
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
```

### 3. 子项目构建文件

**迁移前:**
```kotlin
plugins {
  alias(libs.plugins.kotlin.jvm)
  `maven-publish`
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
      // ... pom 配置
    }
  }
}
```

**迁移后:**
```kotlin
plugins {
  alias(libs.plugins.kotlin.jvm)
  `maven-publish`
  signing  // 新增签名插件
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
      // ... pom 配置
    }
  }
}

signing {
  // 使用环境变量配置签名
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications["maven"])
}
```

### 4. JReleaser 配置 (jreleaser.yml)

**关键配置:**
```yaml
deploy:
  maven:
    mavenCentral:
      kdraft-sonatype:
        active: ALWAYS
        url: https://central.sonatype.com/api/v1/publisher
        stagingRepository: build/staging-deploy
        # 需要的环境变量:
        # JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_USERNAME
        # JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_PASSWORD
```

### 5. GitHub Actions 工作流 (.github/workflows/release.yml)

**迁移前:**
```yaml
- name: Build with Gradle
  run: ./gradlew build

- name: Create GitHub Release with JReleaser
  env:
    JRELEASER_GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  run: ./gradlew jreleaserRelease
```

**迁移后:**
```yaml
- name: Build with Gradle
  run: ./gradlew build

- name: Publish to staging repository
  run: ./gradlew publish
  env:
    ORG_GRADLE_PROJECT_signingKey: ${{ secrets.GPG_PRIVATE_KEY }}
    ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.GPG_PASSPHRASE }}

- name: Run JReleaser
  env:
    JRELEASER_GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    JRELEASER_GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
    JRELEASER_GPG_PUBLIC_KEY: ${{ secrets.GPG_PUBLIC_KEY }}
    JRELEASER_GPG_SECRET_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
    JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_USERNAME: ${{ secrets.SONATYPE_USERNAME }}
    JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_PASSWORD: ${{ secrets.SONATYPE_PASSWORD }}
  run: ./gradlew jreleaserFullRelease
```

## 环境变量和密钥

### 需要配置的 GitHub Secrets

1. **GPG 签名密钥** (Maven Central 必需):
   - `GPG_PRIVATE_KEY`: GPG 私钥 (ASCII-armored format)
   - `GPG_PASSPHRASE`: GPG 密钥密码
   - `GPG_PUBLIC_KEY`: GPG 公钥 (ASCII-armored format)

2. **Sonatype 凭证** (Maven Central 发布):
   - `SONATYPE_USERNAME`: Sonatype 用户名或 token
   - `SONATYPE_PASSWORD`: Sonatype 密码或 token

3. **GitHub Token** (自动提供):
   - `GITHUB_TOKEN`: GitHub Actions 自动提供

### 生成 GPG 密钥

如果你还没有 GPG 密钥，可以使用以下命令生成:

```bash
# 生成新密钥
gpg --gen-key

# 列出密钥
gpg --list-secret-keys --keyid-format=long

# 导出私钥 (ASCII-armored format)
gpg --armor --export-secret-keys YOUR_KEY_ID

# 导出公钥
gpg --armor --export YOUR_KEY_ID

# 发布公钥到密钥服务器
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### Sonatype Central 凭证

1. 访问 https://central.sonatype.com/
2. 注册或登录账户
3. 生成 API Token (推荐) 或使用用户名/密码
4. 将凭证添加到 GitHub Secrets

## 新的发布流程

### 完整发布命令

```bash
# 完整发布 (构建 -> 发布 -> GitHub Release -> Maven Central)
./gradlew jreleaserFullRelease

# 或分步执行:

# 1. 构建项目
./gradlew clean build

# 2. 发布到本地 staging 目录
./gradlew publish

# 3. 执行 JReleaser 完整发布
./gradlew jreleaserFullRelease
```

### 其他有用的 JReleaser 命令

```bash
# 仅创建 GitHub Release
./gradlew jreleaserRelease

# 仅部署到 Maven Central
./gradlew jreleaserDeploy

# 验证配置
./gradlew jreleaserConfig

# 查看环境变量名称
./gradlew jreleaserEnv

# 生成变更日志
./gradlew jreleaserChangelog
```

## 验证清单

在执行发布前，请确认:

- [ ] 所有 GitHub Secrets 已正确配置
- [ ] GPG 公钥已发布到公共密钥服务器
- [ ] Sonatype 账户已验证并有发布权限
- [ ] 版本号已正确设置在 `build.gradle.kts`
- [ ] 项目构建成功: `./gradlew clean build`
- [ ] POM 元数据正确 (名称、描述、许可证等)

## 发布流程说明

### 使用 GitHub Actions 自动发布

1. 创建并推送版本标签:
```bash
git tag v0.0.1
git push origin v0.0.1
```

2. GitHub Actions 将自动:
   - 检出代码
   - 构建项目
   - 签名工件
   - 创建 GitHub Release
   - 发布到 Maven Central

### 本地测试发布

```bash
# 设置环境变量
export ORG_GRADLE_PROJECT_signingKey="YOUR_GPG_PRIVATE_KEY"
export ORG_GRADLE_PROJECT_signingPassword="YOUR_GPG_PASSPHRASE"
export JRELEASER_GITHUB_TOKEN="YOUR_GITHUB_TOKEN"
export JRELEASER_GPG_PASSPHRASE="YOUR_GPG_PASSPHRASE"
export JRELEASER_GPG_PUBLIC_KEY="YOUR_GPG_PUBLIC_KEY"
export JRELEASER_GPG_SECRET_KEY="YOUR_GPG_PRIVATE_KEY"
export JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_USERNAME="YOUR_SONATYPE_USERNAME"
export JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_PASSWORD="YOUR_SONATYPE_PASSWORD"

# 执行发布
./gradlew clean build publish jreleaserFullRelease
```

## 迁移优势

1. **统一发布流程**: JReleaser 提供统一的发布体验，支持多种发布目标
2. **更好的集成**: 原生支持 GitHub Releases、变更日志生成、公告等
3. **现代化 API**: 使用 Sonatype Central Portal 的新 API
4. **更灵活的配置**: 通过 YAML 配置文件实现更清晰的配置
5. **更好的文档**: JReleaser 有详细的文档和活跃的社区支持

## 回滚计划

如果迁移出现问题，可以通过以下步骤回滚:

1. 恢复 `gradle/libs.versions.toml` 中的 `nexus-publish` 插件配置
2. 恢复 `build.gradle.kts` 中的 `nexusPublishing` 配置
3. 从子项目中移除 `signing` 配置
4. 恢复 GitHub Actions 工作流到之前的版本

## 常见问题

### Q: 为什么需要 GPG 签名？
A: Maven Central 要求所有发布的工件必须使用 GPG 签名，以确保工件的完整性和真实性。

### Q: Sonatype Central Portal 和 OSSRH 有什么区别？
A: Sonatype Central Portal 是新的发布平台，提供更简单的 API 和更快的发布流程。OSSRH 是旧的平台，JReleaser 使用新平台。

### Q: 如何验证发布是否成功？
A: 检查以下位置:
- GitHub Releases 页面应该有新的 release
- Maven Central 搜索页面: https://central.sonatype.com/
- JReleaser 日志文件: `build/jreleaser/trace.log`

### Q: 发布失败怎么办？
A: 
1. 检查 JReleaser 日志: `build/jreleaser/trace.log`
2. 验证所有环境变量正确设置
3. 确认 GPG 密钥和 Sonatype 凭证有效
4. 检查网络连接和 API 可用性

## 参考资源

- [JReleaser 官方文档](https://jreleaser.org/guide/latest/)
- [JReleaser GitHub 仓库](https://github.com/jreleaser/jreleaser)
- [Maven Central 发布指南](https://central.sonatype.org/publish/)
- [GPG 签名指南](https://central.sonatype.org/publish/requirements/gpg/)
- [Sonatype Central Portal](https://central.sonatype.com/)

## 支持

如有问题，请访问:
- JReleaser 文档: https://jreleaser.org/guide/latest/
- JReleaser 讨论区: https://github.com/jreleaser/jreleaser/discussions
