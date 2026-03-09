package com.telenav.scoutivi.espeak

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.telenav.scoutivi.tts.espeak.EspeakApi
import com.telenav.scoutivi.tts.espeak.TashkeelApi
import com.telenav.scoutivi.tts.phoneme.aidl.IPhonemeInterface
import com.telenav.scoutivi.tts.phoneme.aidl.PhonemeConfig
import com.telenav.scoutivi.tts.phoneme.aidl.PhonemeResult
import java.util.concurrent.CountDownLatch

/**
 * @Description: class role description
 * @Author: Yan
 * @Date: 2024/7/30 11:18
 */
class PhonemeService : Service() {
    companion object {
        private const val TAG = "TTS::PhonemeService"
    }

    private val sharedPreferencesManager = EspeakSharedPreferenceManager.get()

    private val mBinder = object : IPhonemeInterface.Stub() {
        override fun phoneme(input: String?, espeakVoice: String): PhonemeResult {
            Log.v(TAG, "phoneme input $input")
            if (input == null) {
                return PhonemeResult(emptyList())
            }
            if (isInitialized) {
                val data = mEspeakApi.phoneme(input, 0x03, espeakVoice)
                Log.v(TAG, "output  $data")
                return PhonemeResult(data)
            } else {
                val latch = CountDownLatch(1)
                val resultHolder = arrayOfNulls<PhonemeResult>(1) // Array to hold re
                mInitCallback = {
                    val data = mEspeakApi.phoneme(input, 0x03, espeakVoice)
                    resultHolder[0] = PhonemeResult(data)
                    latch.countDown()
                }
                latch.await()
                return resultHolder[0] ?: PhonemeResult(emptyList())
            }
        }

        override fun tashkeelRun(input: String?): String {
            Log.v(TAG, "phoneme input $input")
            if (input == null) {
                return ""
            }
            if (isInitialized) {
                val tashkeel = mTashkeelApi.run(input)
                Log.v(TAG, "tashkeel output $tashkeel")
                return tashkeel
            } else {
                val latch = CountDownLatch(1)
                val resultHolder = arrayOfNulls<String>(1) // Array to hold re
                mInitCallback = {
                    val tashkeel = mTashkeelApi.run(input)
                    resultHolder[0] = tashkeel
                    latch.countDown()
                }
                latch.await()
                return resultHolder[0] ?: ""
            }
        }

        override fun getConfig(): PhonemeConfig {
            return PhonemeConfig(sharedPreferencesManager.isSaveAudio())
        }
    }

    private val mThread = HandlerThread("phoneme-worker")
    private var mHandler: Handler? = null
    private val mEspeakApi = EspeakApi()
    private val mTashkeelApi = TashkeelApi()
    private var mInitCallback: () -> Unit = { }

    @Volatile private var isInitialized = false
    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        mThread.start()
        mHandler = Handler(mThread.looper)
        mHandler?.post {
            mEspeakApi.init(application)
            mTashkeelApi.init(application)
            isInitialized = true
            mInitCallback.invoke()
            Log.v(TAG, "init success")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTashkeelApi.release()
        mEspeakApi.terminate()
        mHandler?.removeCallbacksAndMessages(null)
        mThread.quitSafely()
    }
}
