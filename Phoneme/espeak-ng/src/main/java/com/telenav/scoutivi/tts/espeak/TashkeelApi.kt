package com.telenav.scoutivi.tts.espeak

import android.content.Context
import android.util.Log
import java.io.File

/**
 * @Description: class role description
 * @Author: Yan
 * @Date: 2024/7/30 10:57
 */
class TashkeelApi {
    @Volatile
    private var mNativePointer = -1L

    companion object {
        private const val TAG = "TashkeelApi"
    }

    fun init(context: Context) {
        if (mNativePointer != -1L) {
            Log.e(TAG, "TashkeelApi already initialized")
            return
        }
        // 先从根目录取（getExternalFilesDir(null)），不可用或为 null 时再从 cache 目录取（getCacheDir）；都不可用时打错误日志并抛异常，并校验 exists/mkdirs/可写
        var baseDir: File? = context.getExternalFilesDir(null)
        if (baseDir == null) {
            baseDir = context.cacheDir
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

        val tashkeelFile = File(baseDir, "model.onnx")
        if (!tashkeelFile.exists()) {
            FileUtils.copyAssetFile(context, "model.onnx", tashkeelFile)
        }
        if (!tashkeelFile.exists() || tashkeelFile.length() == 0L) {
            Log.e(TAG, "Tashkeel model file missing or empty: ${tashkeelFile.absolutePath}")
            return
        }

        mNativePointer = initTashkeel(tashkeelFile.absolutePath)
        if (mNativePointer == -1L) {
            Log.e(TAG, "initTashkeel failed")
            return
        }
        Log.v(TAG, "tashkeel successfully initialized "+ tashkeelFile.absolutePath)
    }

    fun run(text: String): String {
        if (mNativePointer == -1L) return ""
        try {
            val start = System.currentTimeMillis()
            val result = tashkeelRun(mNativePointer, text)
            val end = System.currentTimeMillis()
            Log.v(TAG, "tashkeelRun ${end - start} ms")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "tashkeelRun error ", e)
        }
        return ""
    }

    fun release() {
        release(nativePtr = mNativePointer)
        mNativePointer = -1L
    }

    private external fun initTashkeel(modelPath: String): Long

    private external fun tashkeelRun(nativePtr: Long, text: String): String

    private external fun release(nativePtr: Long)
}