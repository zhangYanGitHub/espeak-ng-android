package com.telenav.scoutivi.tts.espeak

import android.app.Application
import android.util.Log
import java.io.File

/**
 * @Description: class role description
 * @Author: Yan
 * @Date: 2024/4/23 10:47
 */
class EspeakApi {

    companion object {
        private const val TAG = "EspeakApi"
        init {
            System.loadLibrary("ucd")
            System.loadLibrary("espeak-ng")
            System.loadLibrary("espeak-jni")
            System.loadLibrary("onnxruntime")
            System.loadLibrary("piper_phonemize")
        }
    }

    @Volatile
    private var nativePtr: Long = -1L

    fun init(application: Application) {
        if(nativePtr != -1L){
            Log.e(TAG, "EspeakApi already initialized")
            return
        }
        // 先从根目录取（getExternalFilesDir(null)），不可用或为 null 时再从 cache 目录取（getCacheDir）；都不可用时打错误日志并抛异常，并校验 exists/mkdirs/可写
        var baseDir: File? = application.getExternalFilesDir(null)
        if (baseDir == null) {
            baseDir = application.cacheDir
        }
        if (baseDir == null) {
            val msg = "Neither getExternalFilesDir nor getCacheDir is available."
            Log.e(TAG, msg)
            throw IllegalStateException(msg)
        }
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            val msg = "Writable root dir does not exist and mkdirs failed: ${baseDir.absolutePath}"
            Log.e(TAG, msg)
            throw IllegalStateException(msg)
        }
        if (!baseDir.canWrite()) {
            val msg = "Writable root dir is not writable: ${baseDir.absolutePath}"
            Log.e(TAG, msg)
            throw IllegalStateException(msg)
        }
        val espeakDataFile = File(baseDir, "espeak-ng-data")
        baseDir.mkdirs()
        if (!espeakDataFile.exists()) {
            FileUtils.copyAssets(application, "espeak-ng-data", espeakDataFile)
        }
        nativePtr = initEspeakNg(espeakDataFile.absolutePath)
        Log.v(TAG, "espeak-ng init success")
    }

    /**
     * Translates text into phonemes.  Call espeak_SetVoiceByName() first, to select a language.
     *
     *    It returns a pointer to a character string which contains the phonemes for the text up to
     *    end of a sentence, or comma, semicolon, colon, or similar punctuation.
     *
     *    textptr: The address of a pointer to the input text which is terminated by a zero character.
     *       On return, the pointer has been advanced past the text which has been translated, or else set
     *       to NULL to indicate that the end of the text has been reached.
     *
     *
     *    phoneme_mode
     * 	    bit 1:   0=eSpeak's ascii phoneme names, 1= International Phonetic Alphabet (as UTF-8 characters).
     *         bit 7:   use (bits 8-23) as a tie within multi-letter phonemes names
     *         bits 8-23:  separator character, between phoneme names
     */
    fun phoneme(text: String, phonemeModel: Int, espeakVoice: String): List<List<String>> {
        val result =  espeakNgPhoneme(nativePtr, text, phonemeModel,espeakVoice)
        val re = mutableListOf<String>()
        result.forEach {
            re.add(it.joinToString())
        }
        return result
    }

    fun terminate() {
        terminateEspeakNg(nativePtr)
        nativePtr = -1L
    }

    private external fun initEspeakNg(dataPath: String): Long

    private external fun espeakNgPhoneme(nativePtr: Long, text: String, phonemeModel: Int,espeakVoice: String): List<List<String>>

    private external fun terminateEspeakNg(nativePtr: Long)
}