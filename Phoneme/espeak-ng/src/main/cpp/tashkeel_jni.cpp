#include <jni.h>
#include "tashkeel_api.h"

//
// Created by Zhang, Yan (Yan) on 2024/7/30.
//


extern "C"
JNIEXPORT jlong JNICALL
Java_com_telenav_scoutivi_tts_espeak_TashkeelApi_initTashkeel(JNIEnv *env, jobject thiz,
                                                              jstring model_path) {
    auto *api = new TashkeelApi();
    const char *textStr = (env)->GetStringUTFChars(model_path, JNI_FALSE);
    api->init(textStr, env);
    return reinterpret_cast<jlong>(api);
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_telenav_scoutivi_tts_espeak_TashkeelApi_tashkeelRun(JNIEnv *env, jobject thiz,
                                                             jlong native_ptr, jstring text) {

    const char *textStr = (env)->GetStringUTFChars(text, JNI_FALSE);
    auto *api = reinterpret_cast<TashkeelApi *>(native_ptr);
    auto result = api->tashkeelRun(textStr);
    return env->NewStringUTF(result.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_telenav_scoutivi_tts_espeak_TashkeelApi_release(JNIEnv *env, jobject thiz,jlong native_ptr) {
    auto *api = reinterpret_cast<TashkeelApi *>(native_ptr);
    api->release();
}