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
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.snackbar.Snackbar
import fr.enssat.babelblock.delvoye_legal.models.LocaleItem
import fr.enssat.babelblock.delvoye_legal.tools.BlockService
import fr.enssat.babelblock.delvoye_legal.tools.SpeechToTextTool
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val recordAudioRequestCode = 1

    private lateinit var speechToText: SpeechToTextTool
    private lateinit var selectedSpokenLanguage: Locale
    private lateinit var localeAdapter: LocaleRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }

        // Init Toolbar
        setSupportActionBar(myToolbar)
        myToolbar.title = R.string.app_name.toString()

        // Init LocaleList
        initRecyclerView()
        addDataSet()

        // Init STT language to French (first item of Spinner)
        val service = BlockService(this)
        selectedSpokenLanguage = stringToLocale("French")
        speechToText = service.speechToText(selectedSpokenLanguage)


        // Listeners
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

        startTalkButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d("RecoUI", "Started")

                pressTheButton.visibility = View.INVISIBLE // Hide "Press the microphone button ..."
                sentencePronouncedTitle.visibility = View.INVISIBLE // Hide "You said :"
                sentencePronounced.text = "" // Delete old SpeechToText
                sentencePronouncedSpinner.visibility = View.VISIBLE // Display Loading Spinner

                v.performClick()
                Snackbar.make(startTalkButton, "Listening...", Snackbar.LENGTH_SHORT).show()
                speechToText.start(object : SpeechToTextTool.Listener {
                    override fun onResult(text: String, isFinal: Boolean) {
                        if (isFinal) {
                            sentencePronouncedSpinner.visibility = View.INVISIBLE
                            sentencePronouncedTitle.visibility = View.VISIBLE
                            sentencePronounced.visibility = View.VISIBLE
                            sentencePronounced.text = text.capitalize(selectedSpokenLanguage)
                        }
                    }

                    override fun onError() {
                        Snackbar.make(startTalkButton, "An error occurred... Try again !", Snackbar.LENGTH_SHORT).show()
                        sentencePronouncedSpinner.visibility = View.INVISIBLE
                        sentencePronouncedTitle.visibility = View.INVISIBLE
                        sentencePronounced.visibility = View.INVISIBLE
                        pressTheButton.visibility = View.VISIBLE
                    }
                })
            }
            false
        }

        addLanguageButton.setOnClickListener {
            Log.d("addLanguageButton", "BUTTON PRESSED")
            MaterialDialog(this).title(R.string.select_language_to_translate_dialog_title).show {
                listItemsSingleChoice(R.array.languages) { _, _, text ->
                    Log.d("selectedLanguageDialog", "Selected item '$text'")
                    localeAdapter.addItem(LocaleItem(stringToLocale(text.toString())), localeAdapter.itemCount)
                }
            }
        }
    }

    private fun addDataSet() {
        val data = DataSource.createDataSet()
        localeAdapter.submitList(data)
    }

    override fun onDestroy() {
        speechToText.close()
        super.onDestroy()
    }

    /**
     * LocaleRecyclerView initialisation
     */
    private fun initRecyclerView() {
        localeRecyclerView.apply {
            localeRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            localeAdapter = LocaleRecyclerAdapter()
            localeRecyclerView.adapter = localeAdapter
        }
    }

    /**
     * Useful functions
     */
    private fun stringToLocale(s: String): Locale {
        return when (s) {
            "French" -> Locale.FRENCH
            "English" -> Locale.ENGLISH
            "Spanish" -> Locale("es", "ES")
            "Chinese" -> Locale.CHINESE
            "Italian" -> Locale.ITALIAN
            "Japanese" -> Locale.JAPANESE
            "German" -> Locale.GERMAN
            else -> throw Exception("Locale not recognized : $s")
        }
    }

    /**
     * Check permissions
     */
    private fun checkPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), recordAudioRequestCode)
    }

    /**
     * Request Permissions
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == recordAudioRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }
}