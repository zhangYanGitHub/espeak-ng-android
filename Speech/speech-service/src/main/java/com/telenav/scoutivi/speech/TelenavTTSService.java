package com.telenav.scoutivi.speech;

import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.util.Log;
import com.telenav.scoutivi.tts.api.TTSEngine;
import com.telenav.scoutivi.tts.api.callback.UtteranceProgressListener;
import com.telenav.scoutivi.tts.api.model.TTSConfig;
import com.telenav.scoutivi.tts.api.model.TTSLanguage;
import com.telenav.scoutivi.tts.log.VLog;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description: class role description
 * @Author: Yan
 * @Date: 2024/8/5 17:17
 */
public class TelenavTTSService extends TextToSpeechService {

    private static final String TAG = "TTS:TelenavService";

    private final AtomicBoolean isEngineInitialized = new AtomicBoolean(false);
    private TTSLanguage mCurrentLanguage = null;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isEngineInitialized.get()) {
            isEngineInitialized.set(false);
            TTSEngine.get().shutdown();
        }
        Log.v(TAG, "onDestroy");
    }

    @Override
    public int onIsLanguageAvailable(String lang, String country, String variant) {
        TTSLanguage language = TTSLanguage.iso3ToTTSLanguage(lang);
        if (language != null) {
            Log.v(TAG, "onIsLanguageAvailable lang:" + lang + " country:" + country + " variant:" + variant + " -> language:" + language);
        }
        return language == null ? TextToSpeech.LANG_NOT_SUPPORTED : TextToSpeech.LANG_AVAILABLE;
    }

    @Override
    public String[] onGetLanguage() {
        List<TTSLanguage> languages = TTSEngine.get().getSupportedLanguages();
        String[] result = new String[languages.size()];
        for (int i = 0; i < languages.size(); i++) {
            result[i] = languages.get(i).name();
        }
        return result;
    }

    @Override
    public int onLoadLanguage(String lang, String country, String variant) {
        Log.v(TAG, "onLoadLanguage " + lang + " " + country + " " + variant);
        if (lang == null) return TextToSpeech.LANG_NOT_SUPPORTED;

        TTSLanguage language = TTSLanguage.iso3ToTTSLanguage(lang);
        if (language != null) {
            if (mCurrentLanguage != language && mCurrentLanguage != null) {
                TTSEngine.get().shutdown();
                isEngineInitialized.set(false);
            }
            if (!isEngineInitialized.get()) {
                isEngineInitialized.set(true);
                mCurrentLanguage = language;
                // TTSConfig 需要提供完整的构造参数，这里模拟原 Kotlin 的默认参数行为
                // Kotlin: TTSConfig(language = it) -> other params use defaults
                // Java: new TTSConfig(language, new VLog(), true, null)
                TTSEngine.get().init(getApplication(), new TTSConfig(language, new VLog(), true, null), null);
            }
            return TextToSpeech.LANG_AVAILABLE;
        }
        return TextToSpeech.LANG_NOT_SUPPORTED;
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop");
        if (!isEngineInitialized.get()) {
            return;
        }
        TTSEngine.get().stop();
    }

    @Override
    protected void onSynthesizeText(SynthesisRequest request, SynthesisCallback callback) {
        if (request == null) return;
        Log.v(TAG, "onSynthesizeText " + request.getCharSequenceText() + " " + request.getLanguage());

        TTSLanguage iso3ToTTSLanguage = TTSLanguage.iso3ToTTSLanguage(request.getLanguage());
        if (iso3ToTTSLanguage == null) return;

        if (mCurrentLanguage != iso3ToTTSLanguage && mCurrentLanguage != null) {
            TTSEngine.get().shutdown();
            isEngineInitialized.set(false);
        }

        if (!isEngineInitialized.get()) {
            isEngineInitialized.set(true);
            mCurrentLanguage = iso3ToTTSLanguage;
            // 同上，TTSConfig Java 构造函数需补全参数
            TTSEngine.get().init(getApplication(), new TTSConfig(iso3ToTTSLanguage, new VLog(), true, null), null);
        }

        TTSEngine.get().speak(
                request.getCharSequenceText().toString(),
                TTSEngine.QUEUE_FLUSH,
                new UtteranceProgressListener() {
                    @Override
                    public void onStart(int sampleRateInHz, int audioFormat, int channelCount) {
                        Log.v(TAG, "onSynthesizeText onStart");
                        if (callback != null) {
                            callback.start(sampleRateInHz, audioFormat, channelCount);
                        }
                    }

                    @Override
                    public void onDone() {
                        Log.v(TAG, "onSynthesizeText onDone");
                        if (callback != null) {
                            callback.done();
                        }
                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.v(TAG, "onSynthesizeText onError");
                        if (callback != null) {
                            callback.error(errorCode);
                        }
                    }

                    @Override
                    public void onStop(boolean interrupted) {
                        Log.v(TAG, "onSynthesizeText onStop");
                        if (callback != null) {
                            callback.error(TextToSpeech.ERROR_SERVICE);
                        }
                    }
                }
        );
    }
}

