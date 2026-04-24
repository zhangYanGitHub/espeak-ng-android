**English** | [简体中文](README.zh-CN.md)

# espeak-ng-android

Android **APK + background Service** that exposes **espeak-ng G2P (grapheme-to-phoneme)**: turn text into phoneme sequences for a given espeak voice, callable from other apps via **AIDL**.  
This is **not** an Android system `TextToSpeechService` / system TTS engine wrapper.

## Modules

| Module | Role |
|--------|------|
| `Phoneme/espeak-ng` | JNI loads `espeak-ng` and related native libraries; init and phoneme conversion |
| `Phoneme/espeak-server` | Installable **APK** (host app) exporting `PhonemeService` |
| `Phoneme/phoneme-aidl` | AIDL interfaces and data types (`IPhonemeInterface`, etc.) |
| `Phoneme/phoneme-sdk` | Client `PhonemeManager`: bind remote service, call `phoneme` / `tashkeelRun`, etc. |

## Features

- **Offline G2P** via espeak-ng: text + `espeakVoice` (must be a supported **language Identifier**; see below) → nested phoneme lists (`PhonemeService` calls native code).
- Bind `PhonemeService` with an **explicit Intent** and `BIND_AUTO_CREATE`; contracts live in `phoneme-aidl`.
- Extra APIs such as Arabic **tashkeel** (see AIDL and `PhonemeService`).

## Build & run

1. Clone the repo and open the root project in Android Studio.  
2. Build and install the APK from **`Phoneme:espeak-server`** (keep that app/process available so other apps can bind its `Service`).  
3. Integrating apps depend on **`phoneme-aidl`** and **`phoneme-sdk`** (or bind using your own AIDL copy); the server APK must be installed on the device.

The server APK `applicationId` is set in `Phoneme/espeak-server/build.gradle.kts` (**default:** `com.espeak.tts.server`). Use a package name that **matches the installed APK**, with this service **action**:

`com.telenav.scoutivi.tts.PHONEME_SERVICE`

(as declared for `PhonemeService` in `AndroidManifest.xml`).

## Client integration

Initialize from `Application` (or similar), then call:

```java
PhonemeManager.get().init(getApplicationContext());
// After bind + espeak-ng init:
// Second arg: espeak voice name — use the official table "Identifier" column (BCP 47), e.g. en, en-us
List<List<String>> phones = PhonemeManager.get().phoneme("Hello", "en");
```

### `espeakVoice` (language Identifier)

`espeakVoice` is passed to espeak-ng for voice selection. It **must** match an **Identifier** from the upstream language list (e.g. `en`, `en-us`, `cmn`, `ja`), not an ad‑hoc shorthand. Full table:

[espeak-ng/docs/languages.md](https://github.com/espeak-ng/espeak-ng/blob/master/docs/languages.md)

You can also cross-check installed voices with `espeak-ng --voices` on desktop builds.

`PhonemeManager` uses `Intent#setPackage(...)` plus the action above. **Note:** Gradle `applicationId` for `espeak-server` may differ from the package string in `PhonemeManager` / `<queries>` in `phoneme-sdk`. Before shipping, align `Phoneme/espeak-server/build.gradle.kts` with `PhonemeManager.java` so they match the APK you install; otherwise `bindService` fails.
