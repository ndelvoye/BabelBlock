package fr.enssat.babelblock.delvoye_legal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import fr.enssat.babelblock.delvoye_legal.tools.BlockService
import fr.enssat.babelblock.delvoye_legal.tools.SpeechToTextTool
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val recordAudioRequestCode = 1

    private lateinit var speechToText: SpeechToTextTool
    private lateinit var selectedSpokenLanguage: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }

        val service = BlockService(this)
        // Recognize the sentence in selected spoken language "Spinner"
        selectedSpokenLanguage = stringToLocale("French")
        speechToText = service.speechToText(selectedSpokenLanguage)

        startTalkButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d("RecoUI", "Started")
                Snackbar.make(startTalkButton, "You can talk...", Snackbar.LENGTH_SHORT).show()
                sentencePronounced.text = ""
                v.performClick()
                speechToText.start(object : SpeechToTextTool.Listener {
                    override fun onResult(text: String, isFinal: Boolean) {
                        if (isFinal) {
                            sentencePronounced.text = text
                        }
                    }
                })
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d("RecoUI", "Stopped")
                speechToText.stop()
            }
            false
        }

        selectSpokenLanguageSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSpokenLanguage = stringToLocale(selectSpokenLanguageSpinner.selectedItem.toString())
                speechToText = service.speechToText(selectedSpokenLanguage)
                Log.d("Spinner", "Selected spoken language = $selectedSpokenLanguage")
            }
        }
    }

    override fun onDestroy() {
        speechToText.close()
        super.onDestroy()
    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), recordAudioRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == recordAudioRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stringToLocale(s: String): Locale {
        return when(s) {
            "French" -> Locale.FRENCH
            "English" -> Locale.ENGLISH
            "Chinese" -> Locale.CHINESE
            "Italian" -> Locale.ITALIAN
            "Japanese" -> Locale.JAPANESE
            "German" -> Locale.GERMAN
            else -> throw Exception("Locale not recognized : $s")
        }
    }
}