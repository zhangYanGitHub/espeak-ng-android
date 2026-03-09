//
// Created by Zhang, Yan (Yan) on 2024/4/25.
//

#ifndef TTS_ESPEAK_API_H
#define TTS_ESPEAK_API_H

#include <jni.h>
#include "Log.h"
#include <cstring>
#include <limits> // For std::numeric_limits

#include "piper-phonemize/phonemize.hpp"
#include "piper-phonemize/uni_algo.h"
#include <map>
#include <memory>
#include <string>
#include <vector>
#include <stdio.h>
#include <cstring>
#include "espeak_ng.h"
#include <espeak-ng/speak_lib.h>
#include "piper-phonemize/tashkeel.hpp"
#include <string>
#include <locale>

#include <codecvt>

#define PHONEME_MODE_VALL_E_X 0x03
#define LOG_TAG "ESPEAK_API_TAG"

typedef char32_t Phoneme;

class EspeakApi {
private:

    espeak_ng_ERROR_CONTEXT context;

public:

    int init(const char *espeakDataPath);

    std::vector<std::vector<char32_t>>
    textToPhonemes(const char *text, int phonemeMode, const char *espeakVoiceStr);

    void release();

};

#endif //TTS_ESPEAK_API_H