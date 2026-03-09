package com.telenav.scoutivi.tts.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.telenav.scoutivi.tts.app.R
import com.telenav.scoutivi.tts.api.TTSEngine
import com.telenav.scoutivi.tts.api.callback.UtteranceProgressListener
import com.telenav.scoutivi.tts.api.log.Logger
import com.telenav.scoutivi.tts.api.model.TTSConfig
import com.telenav.scoutivi.tts.api.model.TTSLanguage

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private val sampleSentence = mutableMapOf(
        TTSLanguage.DE_DE.name to "Biegen Sie nach 500 Metern links ab und fahren Sie die nächsten 2 Kilometer geradeaus. Ihr Ziel liegt auf der rechten Seite.",
        TTSLanguage.EN_US.name to "Turn left in 500 meters and continue straight for the next 2 kilometers. Your destination will  be on the right.",
        TTSLanguage.IT_IT.name to "Svoltare a sinistra tra 500 metri e proseguire dritto per i prossimi 2 chilometri. La tua destinazione sarà sulla destra.",
        TTSLanguage.FR_FR.name to "Tournez à gauche après 500 mètres et continuez tout droit pendant 2 kilomètres suivants. Votre destination sera sur la droite.",
        TTSLanguage.ZH_CN.name to "500米处左转继续直行2公里。您的目的地就在右侧。",
        TTSLanguage.ES_ES.name to "Gire a la izquierda en 500 metros y continúe recto durante los siguientes 2 kilómetros. Su destino estará a la derecha.",
        TTSLanguage.PT_PT.name to "Vire à esquerda em 500 metros e continue em frente durante os próximos 2 quilómetros. O seu destino estará à direita.",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val languageSpinner: Spinner = findViewById(R.id.languageSpinner)
        val startTTSButton: Button = findViewById(R.id.startTTSButton)
        val stopTTSButton: Button = findViewById(R.id.stopTTSButton)
        val speakButton: Button = findViewById(R.id.speakButton)
        val textField: EditText = findViewById(R.id.textField)
        val logTextView: TextView = findViewById(R.id.logTextView)

        // Set up the Spinner for language selection
        val options = listOf(
            TTSLanguage.DE_DE,
            TTSLanguage.EN_US,
            TTSLanguage.IT_IT,
            TTSLanguage.FR_FR,
            TTSLanguage.ZH_CN,
            TTSLanguage.ES_ES,
            TTSLanguage.PT_PT
        )
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val logger = object : Logger() {
            override fun prints(priority: Int, tag: String, message: String) {
                logTextView.append("$tag $message\n")
                Log.println(priority, tag, message)
            }

            override fun prints(
                priority: Int,
                tag: String,
                message: String,
                throwable: Throwable
            ) {
                logTextView.append("$tag $message $throwable\n")
                Log.w(tag, message, throwable)
            }
        }

        val config = TTSConfig(TTSLanguage.EN_US, logger)

        // Set up button listeners
        startTTSButton.setOnClickListener {
            TTSEngine.get().init(applicationContext, config,null)
        }

        stopTTSButton.setOnClickListener {
            TTSEngine.get().shutdown()
        }

        speakButton.setOnClickListener {
            TTSEngine.get().speak(
                textField.text.toString(),
                TTSEngine.QUEUE_FLUSH,
                object : UtteranceProgressListener {
                    override fun onStart(sampleRateInHz: Int, audioFormat: Int, channelCount: Int) {
                        Log.v(TAG, "onStart $sampleRateInHz $audioFormat $channelCount")
                    }

                    override fun onDone() {
                        Log.v(TAG, "onDone")
                    }

                    override fun onError(errorCode: Int) {
                        Log.v(TAG, "errorCode $errorCode")
                    }

                    override fun onStop(interrupted: Boolean) {
                        Log.v(TAG, "onStop $interrupted")
                    }
                }
            )
        }

        languageSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newLanguage = options[position]
                textField.setText(sampleSentence[newLanguage.name] ?: "")
                TTSEngine.get().shutdown()
                val newConfig = TTSConfig(newLanguage, logger)
                TTSEngine.get().init(applicationContext, newConfig,null)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // 这里可以选择做一些默认操作
            }
        })
    }
}
