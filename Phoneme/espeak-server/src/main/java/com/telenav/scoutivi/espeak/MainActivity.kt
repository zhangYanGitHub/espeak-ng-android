package com.telenav.scoutivi.espeak

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "espeak-server-MainActivity"
    }

    private val sharedPreferencesManager = EspeakSharedPreferenceManager.get()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<CheckBox>(R.id.enable_save_audio).let {
            it.isChecked = sharedPreferencesManager.isSaveAudio()
            android.util.Log.e(TAG,"init isSaveAudio: ${it.isChecked}")
            it.setOnCheckedChangeListener { _, checked ->
                android.util.Log.e(TAG, "checked: $checked")
                sharedPreferencesManager.saveOrUpdateSaveAudio(checked)
            }
        }

    }
}
