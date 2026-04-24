# espeak-ng-android

本仓库在 Android 上以 **APK + 后台 Service** 的形式提供 **espeak-ng 的 G2P（字素到音素，Grapheme-to-Phoneme）** 能力：将文本按指定 espeak 音色转换为音素序列，供其它应用通过 **AIDL** 跨进程调用。  
**不是** Android 系统 `TextToSpeechService` / 系统 TTS 引擎封装。

## 模块说明

| 模块 | 作用 |
|------|------|
| `Phoneme/espeak-ng` | 通过 JNI 加载 `espeak-ng` 等 native 库，完成初始化与音素转换等底层逻辑 |
| `Phoneme/espeak-server` | 可安装 **APK**（宿主应用），注册并对外暴露 `PhonemeService` |
| `Phoneme/phoneme-aidl` | `IPhonemeInterface` 等 AIDL 接口与数据结构 |
| `Phoneme/phoneme-sdk` | 客户端侧 `PhonemeManager`：绑定远端服务并调用 `phoneme` / `tashkeelRun` 等 |

## 功能概要

- 基于 espeak-ng 的 **离线 G2P**：输入文本 + `espeakVoice`（须为 espeak-ng 支持的 **语言 Identifier**，见下文），返回分层音素列表（由服务端 `PhonemeService` 调用 native 实现）。
- 通过 **显式 Intent + `BIND_AUTO_CREATE`** 绑定 `PhonemeService`；接口定义在 `phoneme-aidl`。
- 另提供与阿拉伯语相关的 **tashkeel** 等扩展调用（见 AIDL 与 `PhonemeService` 实现）。

## 构建与运行

1. 克隆本仓库，使用 Android Studio 打开根目录工程。  
2. 构建并安装 **`Phoneme:espeak-server`** 模块生成的 APK（设备上需常驻该应用进程，以便其它应用绑定其 `Service`）。  
3. 需要集成的应用依赖 **`phoneme-aidl`** 与 **`phoneme-sdk`**（或自行按 AIDL 绑定），在目标设备上已安装上述服务端 APK。

服务端 APK 的 `applicationId` 在 `Phoneme/espeak-server/build.gradle.kts` 中配置；**当前默认**为 `com.espeak.tts.server`。绑定 `Service` 时需使用**与所安装 APK 一致**的包名，且 `action` 为：

`com.telenav.scoutivi.tts.PHONEME_SERVICE`

（与 `PhonemeService` 在 `AndroidManifest.xml` 中的声明一致。）

## 客户端集成示例

使用本仓库提供的 SDK 时，在 `Application` 或合适生命周期内初始化并调用：

```java
PhonemeManager.get().init(getApplicationContext());
// 在已绑定且 espeak-ng 初始化完成后：
// 第二个参数为 espeak 音色名，须使用官方文档「Identifier」列中的取值（BCP 47），例如英式英语 en、美式英语 en-us
List<List<String>> phones = PhonemeManager.get().phoneme("Hello", "en");
```

### `espeakVoice`（语言 Identifier）

`phoneme(..., espeakVoice)` 中的 `espeakVoice` 会传给 espeak-ng 选音色，**取值须与 espeak-ng 官方语言表中的 Identifier 一致**（如 `en`、`en-us`、`cmn`、`ja` 等），而不是随意缩写。完整列表与说明见官方文档：

[espeak-ng/docs/languages.md（Languages 与 Identifier 对照表）](https://github.com/espeak-ng/espeak-ng/blob/master/docs/languages.md)

发行版实际可用音色也可在桌面环境执行 `espeak-ng --voices` 核对（与文档表一致思路）。

`PhonemeManager` 内部通过 `Intent#setPackage(...)` 与上述 `action` 绑定服务。**请注意**：`espeak-server` 当前 Gradle 中的 `applicationId` 与 `phoneme-sdk` 里 `PhonemeManager` / `<queries>` 使用的包名可能不一致；集成前请对照 `Phoneme/espeak-server/build.gradle.kts` 与 `PhonemeManager.java`，将二者改为与你要安装的 APK **相同**的包名，否则 `bindService` 会失败。