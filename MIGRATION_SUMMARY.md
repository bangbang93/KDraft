# JReleaser 迁移完成总结

## 迁移概述

已成功将项目从 **Gradle Nexus Publish Plugin** 迁移到 **JReleaser**。

### 变更统计
- 修改文件：6 个
- 新增文档：2 个
- 代码提交：3 个

---

## 一、修改的配置文件

### 1. gradle/libs.versions.toml
```toml
# 迁移前
nexus-publish = "2.0.0"
publish = { id = "io.github.gradle-nexus.publish-plugin", version.ref = "nexus-publish" }

# 迁移后
jreleaser = "1.15.0"
jreleaser = { id = "org.jreleaser", version.ref = "jreleaser" }
```

### 2. build.gradle.kts (根项目)
```kotlin
// 迁移前
plugins {
  alias(libs.plugins.publish)
}
nexusPublishing {
  repositories {
    sonatype { /* ... */ }
  }
}

// 迁移后
plugins {
  alias(libs.plugins.jreleaser)
}
// nexusPublishing 配置已移除
```

### 3. kdraft-annotations/build.gradle.kts & kdraft-processor/build.gradle.kts
```kotlin
// 新增内容
plugins {
  signing  // 新增签名插件
}

signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  if (signingKey != null && signingPassword != null) {
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
  }
}
```

### 4. jreleaser.yml
```yaml
# 新增/更新配置
deploy:
  maven:
    mavenCentral:
      kdraft-sonatype:
        active: ALWAYS
        url: https://central.sonatype.com/api/v1/publisher
        stagingRepository: build/staging-deploy
```

### 5. .github/workflows/release.yml
```yaml
# 迁移前
- name: Create GitHub Release with JReleaser
  run: ./gradlew jreleaserRelease

# 迁移后
- name: Publish to staging repository
  run: ./gradlew publish
  env:
    ORG_GRADLE_PROJECT_signingKey: ${{ secrets.GPG_PRIVATE_KEY }}
    ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.GPG_PASSPHRASE }}

- name: Run JReleaser
  run: ./gradlew jreleaserFullRelease
  env:
    # GPG 签名环境变量
    # Sonatype 凭证
    # GitHub Token
```

---

## 二、新增的文档

### 1. MIGRATION_GUIDE.md (7.3 KB)
详细的迁移指南，包含：
- 迁移前后配置对比
- 环境变量配置说明
- GPG 密钥生成步骤
- Sonatype 账户设置
- 故障排查指南
- 常见问题解答

### 2. RELEASE_GUIDE.md (4.2 KB)
快速发布参考指南，包含：
- GitHub Secrets 配置表格
- 发布流程步骤
- 有用的命令列表
- 验证检查清单
- 版本号规范

---

## 三、需要配置的 GitHub Secrets

在 GitHub 仓库的 Settings → Secrets and variables → Actions 中添加：

| Secret 名称 | 说明 | 获取方法 |
|------------|------|---------|
| `GPG_PRIVATE_KEY` | GPG 私钥 | `gpg --armor --export-secret-keys YOUR_KEY_ID` |
| `GPG_PASSPHRASE` | GPG 密钥密码 | 创建密钥时设置 |
| `GPG_PUBLIC_KEY` | GPG 公钥 | `gpg --armor --export YOUR_KEY_ID` |
| `SONATYPE_USERNAME` | Sonatype 用户名 | https://central.sonatype.com/ |
| `SONATYPE_PASSWORD` | Sonatype 密码/Token | https://central.sonatype.com/ |

**注意**: `GITHUB_TOKEN` 由 GitHub Actions 自动提供，无需配置。

---

## 四、新的发布流程

### 自动发布（推荐）

```bash
# 1. 更新版本号
# 编辑 build.gradle.kts 中的 version = "x.y.z"

# 2. 提交并打标签
git add build.gradle.kts
git commit -m "chore: bump version to x.y.z"
git push

git tag vx.y.z
git push origin vx.y.z

# 3. GitHub Actions 自动执行：
#    - 构建项目
#    - 签名所有 JAR 文件
#    - 创建 GitHub Release（附带变更日志）
#    - 发布到 Maven Central
```

### 手动发布（本地测试）

```bash
# 设置环境变量
export ORG_GRADLE_PROJECT_signingKey="$(cat private-key.asc)"
export ORG_GRADLE_PROJECT_signingPassword="your-passphrase"
export JRELEASER_GITHUB_TOKEN="your-github-token"
export JRELEASER_GPG_PASSPHRASE="your-passphrase"
export JRELEASER_GPG_PUBLIC_KEY="$(cat public-key.asc)"
export JRELEASER_GPG_SECRET_KEY="$(cat private-key.asc)"
export JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_USERNAME="your-username"
export JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_PASSWORD="your-password"

# 执行发布
./gradlew clean build publish jreleaserFullRelease
```

---

## 五、命令对比

| 操作 | 迁移前 | 迁移后 |
|-----|-------|-------|
| 发布到 staging | `./gradlew publishToSonatype` | `./gradlew publish` |
| 关闭并发布 | `./gradlew closeAndReleaseSonatypeStagingRepository` | 集成在 jreleaserFullRelease 中 |
| 创建 GitHub Release | 手动或单独脚本 | `./gradlew jreleaserRelease` |
| 完整发布流程 | 多个命令 | `./gradlew jreleaserFullRelease` |
| 查看配置 | 不支持 | `./gradlew jreleaserConfig` |

---

## 六、迁移优势

### 功能优势
1. ✅ **统一发布流程**: 一条命令完成 GitHub Release + Maven Central 发布
2. ✅ **自动变更日志**: 基于 conventional commits 自动生成
3. ✅ **现代化 API**: 使用 Sonatype Central Portal 新 API
4. ✅ **更好的配置**: YAML 配置文件，清晰易懂
5. ✅ **更多功能**: 支持公告、文件上传、多平台发布等

### 开发体验
1. ✅ **详细文档**: 提供完整的迁移和发布指南
2. ✅ **安全性**: 通过环境变量管理敏感信息
3. ✅ **可调试**: 详细的日志输出和配置验证
4. ✅ **容错性**: 签名配置支持空值，本地构建不受影响

---

## 七、验证清单

### 配置验证
- [x] 构建成功: `./gradlew clean build` ✅
- [x] JReleaser 任务可用: `./gradlew tasks --group=jreleaser` ✅
- [x] 配置有效: 所有必需的配置项已设置 ✅
- [x] 代码审查: 通过代码审查，添加空值检查 ✅

### 下一步操作（用户需要完成）
- [ ] 生成 GPG 密钥对
- [ ] 将 GPG 公钥发布到密钥服务器
- [ ] 在 Sonatype Central 注册账户并验证 namespace
- [ ] 在 GitHub 配置所有必需的 Secrets
- [ ] 测试发布流程（可以先测试非生产环境）

---

## 八、快速开始指南

### 首次发布前的准备

1. **生成 GPG 密钥**
```bash
gpg --gen-key
gpg --list-secret-keys --keyid-format=long
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc
gpg --armor --export YOUR_KEY_ID > public-key.asc
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

2. **注册 Sonatype 账户**
- 访问: https://central.sonatype.com/
- 注册并验证 namespace: `com.bangbang93.kdraft`
- 生成 API Token

3. **配置 GitHub Secrets**
- 进入仓库 Settings → Secrets and variables → Actions
- 添加上述表格中的 5 个 secrets

4. **测试发布**
```bash
# 更新版本号为测试版本
git tag v0.0.1-test
git push origin v0.0.1-test
# 观察 GitHub Actions 执行结果
```

---

## 九、故障排查

### 常见问题

**Q: 签名失败，提示找不到密钥**
- 检查 `GPG_PRIVATE_KEY` 格式是否为 ASCII-armored
- 确认 `GPG_PASSPHRASE` 正确

**Q: Maven Central 发布失败**
- 验证 Sonatype 凭证是否正确
- 确认 namespace 已在 Sonatype Central 验证
- 检查 POM 信息是否完整

**Q: 本地构建报签名错误**
- 这是正常的，本地构建时签名配置会跳过（因为 null 检查）
- 如需本地测试签名，设置 `signingKey` 和 `signingPassword` 环境变量

**Q: 如何查看详细日志**
```bash
# 本地
cat build/jreleaser/trace.log

# GitHub Actions
下载 jreleaser-logs artifact
```

---

## 十、相关资源

- 📖 [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md) - 详细迁移指南
- 📖 [RELEASE_GUIDE.md](./RELEASE_GUIDE.md) - 发布快速参考
- 🔗 [JReleaser 官方文档](https://jreleaser.org/guide/latest/)
- 🔗 [Maven Central 发布指南](https://central.sonatype.org/publish/)
- 🔗 [项目仓库](https://github.com/bangbang93/KDraft)

---

## 迁移完成状态

✅ **所有迁移任务已完成**
- 插件配置已更新
- 签名配置已添加
- CI/CD 工作流已更新
- 文档已完善
- 构建测试通过

**下一步**: 配置 GitHub Secrets 后即可开始使用新的发布流程！
