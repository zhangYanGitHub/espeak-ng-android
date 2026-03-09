
#include "espeak_api.h"
//
// Created by Zhang, Yan (Yan) on 2024/4/23.
//


// Convert a single char32_t to a Java String
jstring toJavaString(JNIEnv *env, char32_t codePoint) {
    if (codePoint <= 0xFFFF) {
        // Single char
        jchar charValue = static_cast<jchar>(codePoint);
        return env->NewString(&charValue, 1);
    } else if (codePoint <= 0x10FFFF) {
        // Surrogate pair
        jchar high = static_cast<jchar>(((codePoint - 0x10000) >> 10) + 0xD800);
        jchar low = static_cast<jchar>((codePoint - 0x10000) & 0x3FF) + 0xDC00;
        jchar chars[2] = { high, low };
        return env->NewString(chars, 2);
    } else {
        return env->NewString(nullptr, 0);  // Invalid code point
    }
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_telenav_scoutivi_tts_espeak_EspeakApi_initEspeakNg(JNIEnv *env, jobject thiz,
                                                            jstring data_path) {
    const char *espeak_ng_data_path = (env)->GetStringUTFChars(data_path, JNI_FALSE);
    auto *espeakApi = new EspeakApi();
    espeakApi->init(espeak_ng_data_path);
    env->ReleaseStringUTFChars(data_path, espeak_ng_data_path);
    return reinterpret_cast<jlong>(espeakApi);

}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_telenav_scoutivi_tts_espeak_EspeakApi_espeakNgPhoneme(JNIEnv *env, jobject thiz,
                                                               jlong nativePtr,
                                                               jstring text,
                                                               jint phonemeMode,
                                                               jstring espeakVoice) {
    const char *textStr = env->GetStringUTFChars(text, JNI_FALSE);
    const char *espeakVoiceStr = env->GetStringUTFChars(espeakVoice, JNI_FALSE);
    auto *api = reinterpret_cast<EspeakApi *>(nativePtr);
    auto phonemeListArray = api->textToPhonemes(textStr, phonemeMode, espeakVoiceStr);

    env->ReleaseStringUTFChars(text, textStr);
    env->ReleaseStringUTFChars(espeakVoice, espeakVoiceStr);

    // Create Java List<List<String>> object
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    jobject outerList = env->NewObject(arrayListClass, arrayListConstructor);

    for (const auto& innerVector : phonemeListArray) {
        jobject innerList = env->NewObject(arrayListClass, arrayListConstructor);

        for (const auto& codePoint : innerVector) {
            jstring str = toJavaString(env, codePoint);
            env->CallBooleanMethod(innerList, addMethod, str);
            env->DeleteLocalRef(str);
        }

        env->CallBooleanMethod(outerList, addMethod, innerList);
        env->DeleteLocalRef(innerList);
    }

    // Check for exceptions
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
    }
    return outerList;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_telenav_scoutivi_tts_espeak_EspeakApi_terminateEspeakNg(JNIEnv *env, jobject thiz,
                                                                 jlong nativePtr) {
    auto *api = reinterpret_cast<EspeakApi *>(nativePtr);
    api->release();
}