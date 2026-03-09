#include "espeak_api.h"

int EspeakApi::init(const char *espeakDataPath) {
    espeak_ng_InitializePath(espeakDataPath);

    espeak_ng_STATUS status;

    status = espeak_ng_Initialize(&context);
    char message[1024];
    if (status != ENS_OK) {
        espeak_ng_GetStatusCodeMessage(status, message, 1024);
        LOGE("espeak_ng_Initialize error %s", message);
        goto END;
    }
    memset(message, 0, sizeof(message));

    char device[1024];
    status = espeak_ng_InitializeOutput(ENOUTPUT_MODE_SYNCHRONOUS, 1024, device);
    if (status != ENS_OK) {
        espeak_ng_GetStatusCodeMessage(status, message, 1024);
        LOGE("espeak_ng_InitializeOutput error %s", message);
        goto END;
    }
    memset(message, 0, sizeof(message));

    status = espeak_ng_SetVoiceByName("en");
    if (status != ENS_OK) {
        espeak_ng_GetStatusCodeMessage(status, message, 1024);
        LOGV("espeak_ng_SetVoiceByName error %s", message);
        goto END;
    }

    END:
    return status;
}

std::vector<std::vector<char32_t>>
EspeakApi::textToPhonemes(const char *text, int phonemeMode, const char *espeakVoiceStr) {

    std::vector<std::vector<Phoneme>> phonemes;
    if (text == nullptr || strlen(text) == 0 || espeakVoiceStr == nullptr || strlen(espeakVoiceStr) == 0) {
        LOGE("Text or eSpeak voice string is empty.");
        return phonemes;
    }

    try {
        piper::eSpeakPhonemeConfig piperConfig;
        piperConfig.voice = espeakVoiceStr;
        piper::phonemize_eSpeak(text, piperConfig, phonemes);
    } catch (const std::exception &e) {
        LOGE("Error in textToPhonemes: %s", e.what());
    }


    return phonemes;
}

void EspeakApi::release() {
    espeak_ERROR espeakError = espeak_Cancel();
    if (EE_OK != espeakError) {
        LOGE("release espeak_Cancel error: %X", espeakError);
    }
    espeak_ng_ClearErrorContext(&context);
    espeakError = espeak_Terminate();
    if (EE_OK != espeakError) {
        LOGE("release espeak_Terminate error: %X", espeakError);
    }
}
