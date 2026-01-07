# 发布指南

本文档说明如何使用 JReleaser 发布新版本到 Maven Central 和 GitHub Releases。

## 前置要求

### 1. 配置 GitHub Secrets

在 GitHub 仓库设置中添加以下 secrets:

| Secret 名称 | 说明 | 如何获取 |
|------------|------|---------|
| `GPG_PRIVATE_KEY` | GPG 私钥 (ASCII-armored) | `gpg --armor --export-secret-keys YOUR_KEY_ID` |
| `GPG_PASSPHRASE` | GPG 密钥密码 | 创建 GPG 密钥时设置的密码 |
| `GPG_PUBLIC_KEY` | GPG 公钥 (ASCII-armored) | `gpg --armor --export YOUR_KEY_ID` |
| `SONATYPE_USERNAME` | Sonatype 用户名或 Token | 从 https://central.sonatype.com/ 获取 |
| `SONATYPE_PASSWORD` | Sonatype 密码或 Token | 从 https://central.sonatype.com/ 获取 |

**注意**: `GITHUB_TOKEN` 由 GitHub Actions 自动提供，无需手动配置。

### 2. 生成 GPG 密钥（如果没有）

```bash
# 生成密钥
gpg --gen-key

# 查看密钥 ID
gpg --list-secret-keys --keyid-format=long

# 导出私钥
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc

# 导出公钥
gpg --armor --export YOUR_KEY_ID > public-key.asc

# 发布公钥到密钥服务器
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

### 3. 注册 Sonatype Central 账户

1. 访问 https://central.sonatype.com/
2. 注册账户
3. 验证 namespace（例如：`com.bangbang93.kdraft`）
4. 生成 API Token（推荐）或记录用户名/密码

## 发布新版本

### 自动发布（推荐）

1. **更新版本号**

编辑 `build.gradle.kts`:
```kotlin
allprojects {
  group = "com.bangbang93.kdraft"
  version = "0.0.2"  // 更新版本号
}
```

2. **提交更改**
```bash
git add build.gradle.kts
git commit -m "chore: bump version to 0.0.2"
git push
```

3. **创建并推送标签**
```bash
git tag v0.0.2
git push origin v0.0.2
```

4. **等待自动发布**

GitHub Actions 将自动执行以下步骤:
- 构建项目
- 签名所有 JAR 文件
- 创建 GitHub Release
- 发布到 Maven Central

查看进度: https://github.com/bangbang93/KDraft/actions

### 手动发布（本地）

如果需要在本地测试发布流程:

```bash
# 1. 设置环境变量
export ORG_GRADLE_PROJECT_signingKey="$(cat private-key.asc)"
export ORG_GRADLE_PROJECT_signingPassword="YOUR_GPG_PASSPHRASE"
export JRELEASER_GITHUB_TOKEN="YOUR_GITHUB_TOKEN"
export JRELEASER_GPG_PASSPHRASE="YOUR_GPG_PASSPHRASE"
export JRELEASER_GPG_PUBLIC_KEY="$(cat public-key.asc)"
export JRELEASER_GPG_SECRET_KEY="$(cat private-key.asc)"
export JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_USERNAME="YOUR_USERNAME"
export JRELEASER_MAVENCENTRAL_KDRAFT_SONATYPE_PASSWORD="YOUR_PASSWORD"

# 2. 执行完整发布
./gradlew clean build publish jreleaserFullRelease
```

## 有用的命令

```bash
# 仅构建，不发布
./gradlew clean build

# 验证 JReleaser 配置
./gradlew jreleaserConfig

# 查看所有 JReleaser 任务
./gradlew tasks --group=jreleaser

# 仅创建 GitHub Release（不发布到 Maven Central）
./gradlew jreleaserRelease

# 仅发布到 Maven Central（不创建 GitHub Release）
./gradlew jreleaserDeploy

# 查看需要的环境变量
./gradlew jreleaserEnv

# 查看 JReleaser 日志
cat build/jreleaser/trace.log
```

## 验证发布

### GitHub Release
访问: https://github.com/bangbang93/KDraft/releases

应该能看到新的 release，包含:
- 自动生成的变更日志
- 所有 JAR 文件（annotations 和 processor）
- sources 和 javadoc JAR

### Maven Central
访问: https://central.sonatype.com/search?q=com.bangbang93.kdraft

或在项目中使用:
```kotlin
dependencies {
    implementation("com.bangbang93.kdraft:kdraft-annotations:0.0.2")
    ksp("com.bangbang93.kdraft:kdraft-processor:0.0.2")
}
```

**注意**: Maven Central 同步可能需要几分钟到几小时。

## 故障排查

### 构建失败

1. 检查 GitHub Actions 日志
2. 确认所有 secrets 已正确配置
3. 验证版本号格式正确
4. 确保代码编译通过: `./gradlew clean build`

### 签名失败

1. 验证 GPG 密钥格式（必须是 ASCII-armored）
2. 确认 GPG_PASSPHRASE 正确
3. 检查密钥是否已过期: `gpg --list-keys`

### Maven Central 发布失败

1. 验证 Sonatype 凭证
2. 确认 namespace 已验证
3. 检查 POM 信息是否完整（名称、描述、许可证、开发者、SCM）
4. 查看 JReleaser 日志: `build/jreleaser/trace.log`

### 查看详细日志

```bash
# GitHub Actions 中的日志
- 在 Actions 页面查看工作流运行日志
- 下载 jreleaser-logs artifact 查看详细日志

# 本地日志
cat build/jreleaser/trace.log
```

## 发布检查清单

发布前确认:

- [ ] 版本号已更新
- [ ] 代码已提交并推送
- [ ] 所有测试通过: `./gradlew test`
- [ ] 构建成功: `./gradlew build`
- [ ] GitHub Secrets 已配置
- [ ] GPG 公钥已发布到密钥服务器
- [ ] Sonatype 账户已验证

## 版本号规范

遵循语义化版本 (Semantic Versioning):

- **主版本号 (MAJOR)**: 不兼容的 API 变更
- **次版本号 (MINOR)**: 向后兼容的功能新增
- **修订号 (PATCH)**: 向后兼容的问题修正

示例:
- `0.0.1` → `0.0.2`: 修复 bug
- `0.0.2` → `0.1.0`: 添加新功能
- `0.1.0` → `1.0.0`: 第一个稳定版本

## 相关资源

- [JReleaser 文档](https://jreleaser.org/guide/latest/)
- [Maven Central 发布指南](https://central.sonatype.org/publish/)
- [语义化版本规范](https://semver.org/lang/zh-CN/)
- [项目仓库](https://github.com/bangbang93/KDraft)
