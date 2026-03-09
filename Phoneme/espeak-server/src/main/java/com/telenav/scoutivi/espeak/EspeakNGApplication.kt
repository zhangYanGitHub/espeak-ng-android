package com.telenav.scoutivi.espeak

import android.app.Application

/**
 * @Description: class role description
 * @Author: Yan
 * @Date: 2024/7/26 19:28
 */
class EspeakNGApplication:Application() {
    companion object {
        private const val TAG = "EspeakNGApplication"
    }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.i(TAG, "EspeakNGApplication onCreate")
        EspeakSharedPreferenceManager.get().init(this)
    }

}
