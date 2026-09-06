# 来电身份识别

一个 Android 本机来电识别应用。来电时读取来电号码，匹配本机通讯录和用户导入的号码资料库，并对无可靠身份来源的号码给出“疑似骚扰”提示。

## 功能

- Android 10+ 使用 `CallScreeningService` 处理来电，需要把应用设为系统的默认来电识别应用。
- 低版本 Android 作为兜底方案监听 `PHONE_STATE`。
- 匹配本机通讯录，并尝试读取已同步到通讯录的微信资料。
- 支持用户导入本地资料库，格式为每行：`手机号,微信ID,昵称,备注`。
- 来电时展示高优先级通知，并在应用内保留最近 100 条识别记录。
- 通知和悬浮按钮都支持“跳微信”，点击后复制号码并启动微信。
- 号码转化规则支持用户自定义：每行 `前缀=>替换内容`，替换内容留空则删除前缀，例如 `116451=>`。
- 应用内支持检查 GitHub Releases 更新。
- 检查到新版本后，Android 可通过国内镜像下载 APK；也可以回退到 GitHub 发布页。
- 所有号码与资料保存在本机 `SharedPreferences`，没有网络上报。

## 构建

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

构建产物在 `app/build/outputs/apk/debug/phone-check-{版本号}-debug.apk`。

## iOS

iOS 端提供 SwiftUI 基础工程，包含号码规则和 GitHub 更新检查。生成 Xcode 工程可用：

```bash
cd ios
xcodegen generate
```

iOS 系统限制比 Android 更严格，来电身份识别不能完全复用 Android 的 CallScreeningService 机制；实际接入需要走 CallKit，并受苹果审核限制。

## 更新检查

Android 会请求 GitHub 的 `releases/latest` 接口，比较 `tag_name` 与本地版本号，并优先选择 Release 里的 `phone-check-{版本号}-debug.apk` 资产。下载时可走 `GitHubMirror` 中的国内镜像。需要先在 GitHub 仓库创建 Release，版本号建议使用 `v1.3.0` 这类格式。

## CI Release

推送 `v*` 标签或在 GitHub Actions 手动触发 `Build release packages` 后，工作流会构建：

1. Android：可安装的 debug 签名 APK。
2. iOS：未签名 `.ipa`。它只能作为构建产物，安装到真机前仍需要用你的 Apple 开发者证书重新签名。

如果要在 CI 里直接导出已签名 iOS 包，请在仓库 Secrets 中配置：

- `IOS_P12_BASE64`
- `IOS_P12_PASSWORD`
- `IOS_PROFILE_BASE64`
- `APPLE_TEAM_ID`
- `KEYCHAIN_PASSWORD`

可选：

- `IOS_EXPORT_METHOD`：默认 `ad-hoc`
- `IOS_SIGNING_IDENTITY`：默认 `Apple Distribution`

标签推送成功后，工作流会把两个产物一起上传到对应的 GitHub Release。

## 使用

1. 安装 APK，进入应用，授予通讯录、电话状态和通知权限。
2. 点击“设为默认来电识别应用”，在系统弹窗中确认授权。
3. 如需补充号码与微信资料，粘贴到资料库输入框并保存。
4. 来电时通过通知查看身份结果；应用内可查看识别记录。
5. 如需悬浮按钮，点击“授权悬浮窗”。

## 重要边界

微信没有公开的“手机号反查微信账号” API。本应用不访问、不爬取、不上传微信数据，只能识别三类信息：

1. 本机通讯录联系人；
2. 微信同步到系统通讯录的资料；
3. 用户自己导入的授权号码资料库。

因此，未匹配的号码只会显示“疑似骚扰”，不会伪造微信账号结论。如果后续有合法授权的反查数据源，可以把 `CallerIdentityResolver` 中的查询层扩展为可配置服务。

“打开微信查询”只能在用户点击通知后复制号码并打开微信；Android 不允许第三方应用自动操控微信的搜索框，微信也没有提供可自动进入搜索结果的公开 API。因此最后一步仍需在微信里手动粘贴并搜索。

号码规则会先经过系统号码标准化，再按用户规则从长到短匹配一次前缀。匹配后的号码会同时用于联系人匹配、资料库匹配和复制到微信。
