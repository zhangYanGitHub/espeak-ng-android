package com.telenav.scoutivi.tts.espeak

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FileUtils {
    private const val TAG = "FileUtils"

    // 递归复制 Assets 文件夹
    fun copyAssets(context: Context, assetsFolder: String, destinationFolder: File) {
        // 获取 AssetManager
        val assetManager = context.assets
        // 列出 assets 文件夹中的所有文件和子文件夹
        val assetsList = assetManager.list(assetsFolder) ?: return

        // 创建目标文件夹
        if (!destinationFolder.exists()) {
            if (!destinationFolder.mkdirs()) {
                Log.e(TAG, "Failed to create destination folder!")
                return
            }
        }

        // 遍历 assets 文件夹中的所有文件和子文件夹
        for (asset in assetsList) {
            // 构建 assets 中文件/文件夹的路径
            val assetPath = if (assetsFolder.isNotEmpty()) "$assetsFolder/$asset" else asset
            // 判断是否为文件夹
            val isDirectory = try {
                assetManager.list(assetPath)?.isNotEmpty()
            } catch (e: Exception) {
                false
            }

            // 如果是文件夹，则递归复制文件夹
            if (isDirectory == true) {
                copyAssets(context, assetPath, File(destinationFolder, asset))
            } else {
                // 如果是文件，则复制文件
                copyAssetFile(context, assetPath, File(destinationFolder, asset))
            }
        }
    }

    // 复制单个文件
    fun copyAssetFile(context: Context, assetFilePath: String, destinationFile: File) {
        try {
            destinationFile.parentFile?.let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
            val inputStream: InputStream = context.assets.open(assetFilePath)
            val outputStream: OutputStream = destinationFile.outputStream()

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy file: $assetFilePath")

        }
    }

    fun loadTextFromAsset(context: Context, filename: String): String? {
        var json: String? = null
        try {
            // 从 assets 目录中读取 JSON 文件
            val inputStream = context.assets.open(filename)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        // 将 JSON 字符串解析为 JSONObject
        return json
    }


    /**
     * 保存音频数据为 WAV 文件
     *
     * @param audioData 音频原始数据
     * @param sampleRate 采样率（例如 16000）
     * @param numChannels 声道数量（例如 AudioFormat.CHANNEL_OUT_MONO）
     * @param bitsPerSample 采样位深（例如 AudioFormat.ENCODING_PCM_16BIT）
     * @param outputFile 输出文件
     */
    fun saveAudioAsWav(
        audioData: ByteArray,
        sampleRate: Int,
        numChannels: Int,
        bitsPerSample: Int,
        outputFile: File
    ) {
        val header = createWavHeader(audioData.size, sampleRate, numChannels, bitsPerSample)
        try {
            FileOutputStream(outputFile).use { fos ->
                fos.write(header)
                fos.write(audioData)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 创建 WAV 文件头
     *
     * @param dataSize 音频数据大小
     * @param sampleRate 采样率
     * @param numChannels 声道数量
     * @param bitsPerSample 采样位深
     * @return WAV 文件头
     */
    private fun createWavHeader(dataSize: Int, sampleRate: Int, numChannels: Int, bitsPerSample: Int): ByteArray {
        val totalDataLen = dataSize + 36
        val byteRate = sampleRate * numChannels * bitsPerSample / 8
        val blockAlign = numChannels * bitsPerSample / 8

        return ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16) // Sub-chunk size, 16 for PCM
            putShort(1.toShort()) // AudioFormat, 1 for PCM
            putShort(numChannels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(bitsPerSample.toShort())
            put("data".toByteArray())
            putInt(dataSize)
        }.array()
    }
}
