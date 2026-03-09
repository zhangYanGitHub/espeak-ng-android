# espeak-ng-android

This project is an Android Text-To-Speech (TTS) service wrapper for the `espeak-ng` engine. It allows Android applications to utilize `espeak-ng` for offline speech synthesis.

## Project Structure

- `Speech/speech-service`: The Android TTS Service implementation. This service binds to the Android system's TTS framework.
- `Speech/sample`: A sample application to test the TTS engine with various languages and text inputs.
- `Phoneme/espeak-ng`: The core native integration wrapper for the `espeak-ng` engine (handling text-to-phoneme and synthesis logic via JNI).
- `Phoneme/espeak-server`: The background server/service handling the `espeak-ng` capabilities directly.
- `Phoneme/phoneme-aidl` & `Phoneme/phoneme-sdk`: Defines the AIDL interfaces and SDK used to interact with the underlying phoneme extraction and TTS capabilities.

## Features

- Implements the standard Android `TextToSpeechService`.
- Supports multiple languages provided by the `espeak-ng` engine.
- Includes a sample app to test speech synthesis in real-time.

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the `sample` module on an Android device or emulator.
4. Select a language from the dropdown and click "Speak" to test the synthesis.

## Integration

To use this TTS service in your own Android application, you can bind to it using the standard Android `TextToSpeech` API:

```java
TextToSpeech tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setEngineByPackageName("com.espeak.tts.engine");
            tts.speak("Hello world", TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
});
```

## Note
This project has been stripped of other unrelated TTS models (such as ONNX/VITS models) and focuses solely on the `espeak-ng` integration.
