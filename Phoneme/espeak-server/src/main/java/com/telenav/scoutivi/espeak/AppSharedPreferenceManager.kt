package com.telenav.scoutivi.espeak

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class EspeakSharedPreferenceManager {
    companion object {
        const val SHARED_PRE_KEY_SAVE_AUDIO = "save_audio_file"

        @JvmStatic
        fun get(): EspeakSharedPreferenceManager {
            return Holder.instance
        }
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak") val instance = EspeakSharedPreferenceManager()
    }

    private lateinit var sharedPref: SharedPreferences

    fun init(context: Context) {
        sharedPref  =
            context.getSharedPreferences("espeak_settings", Context.MODE_PRIVATE)
    }

    fun saveOrUpdateSaveAudio(save: Boolean) {
        sharedPref.edit().apply {
            putBoolean(SHARED_PRE_KEY_SAVE_AUDIO, save)
        }.apply()
    }

    fun isSaveAudio(): Boolean {
        return sharedPref.getBoolean(SHARED_PRE_KEY_SAVE_AUDIO, false)
    }
}
