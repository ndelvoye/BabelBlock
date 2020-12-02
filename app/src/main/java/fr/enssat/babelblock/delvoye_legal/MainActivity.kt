package fr.enssat.babelblock.delvoye_legal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.snackbar.Snackbar
import fr.enssat.babelblock.delvoye_legal.models.LocaleItem
import fr.enssat.babelblock.delvoye_legal.tools.BlockService
import fr.enssat.babelblock.delvoye_legal.tools.SpeechToTextTool
import fr.enssat.babelblock.delvoye_legal.tools.TextToSpeechTool
import fr.enssat.babelblock.delvoye_legal.utils.LocaleUtils
import fr.enssat.babelblock.delvoye_legal.workers.TranslateWorker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_locale_list_item.*
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {
    private val recordAudioRequestCode = 1

    private lateinit var speechToText: SpeechToTextTool
    private lateinit var selectedSpokenLanguage: Locale
    private lateinit var localeAdapter: LocaleRecyclerAdapter
    private lateinit var speaker: TextToSpeechTool

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Init Toolbar
        setSupportActionBar(myToolbar)
        myToolbar.title = R.string.app_name.toString()

        // Init LocaleList
        initRecyclerView()
        addDataSet()



        // Init STT language to French (first item of Spinner)
        val service = BlockService(this)
        selectedSpokenLanguage = LocaleUtils.stringToLocale("French")
        speechToText = service.speechToText(selectedSpokenLanguage)

        // Init TTS
        speaker = service.textToSpeech()


        // Listeners
        selectSpokenLanguageSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSpokenLanguage =
                        LocaleUtils.stringToLocale(selectSpokenLanguageSpinner.selectedItem.toString())
                    speechToText = service.speechToText(selectedSpokenLanguage)
                    Timber.d("selectedSpokenLanguage = $selectedSpokenLanguage")
                }
            }

        textToSpeech_locale_button.setOnClickListener{
            val text = chain_translation_text.text.toString()
            speaker.speak(text)
        }

        startTalkButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Timber.d("startTalkButton ACTION_DOWN")

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
                            val localeItemList = localeAdapter.getList()
                            for (localeItem in localeItemList) {
                                val index = localeAdapter.getList().indexOf(localeItem)
                                if (index == 0) {
                                    setOneTimeWorkRequest(
                                        0,
                                        sentencePronounced.text as String,
                                        selectedSpokenLanguage,
                                        localeItem.locale
                                    )
                                } else {
                                    val previousItem: LocaleItem = localeItemList[index - 1]
                                    setOneTimeWorkRequest(
                                        index,
                                        sentencePronounced.text as String,
                                        previousItem.locale,
                                        localeItem.locale
                                    )
                                }
                            }
                        }
                    }

                    override fun onError() {
                        Snackbar.make(
                            startTalkButton,
                            "An error occurred... Try again !",
                            Snackbar.LENGTH_SHORT
                        ).show()
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
            Timber.d("addLanguageButton CLICKED")
            MaterialDialog(this).title(R.string.select_language_to_translate_dialog_title).show {
                listItemsSingleChoice(R.array.languages) { _, _, text ->
                    Timber.d("Selected language '$text'")

                    localeAdapter.addItem(
                        LocaleItem(
                            LocaleUtils.stringToLocale(text.toString()),
                            "",
                            WorkInfo.State.BLOCKED
                        ), localeAdapter.itemCount
                    )
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
        speaker.close()
        super.onDestroy()
    }

    /**
     * LocaleRecyclerView initialisation
     */
    private fun initRecyclerView() {
        localeRecyclerView.apply {
            localeAdapter = LocaleRecyclerAdapter()
            localeRecyclerView.adapter = localeAdapter
            localeRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setOneTimeWorkRequest(
        pos: Int,
        sentenceToTranslate: String,
        from: Locale,
        to: Locale
    ) {
        val workManager = WorkManager.getInstance(applicationContext)
        // val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val data = Data.Builder()
            .putString("sentenceToTranslate", sentenceToTranslate)
            .putString("from", from.language)
            .putString("to", to.language)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(TranslateWorker::class.java)
            //.setConstraints(constraints)
            .setInputData(data)
            .build()
        workManager.enqueue(oneTimeWorkRequest)
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observe(this, {
                if (it.state.isFinished) {
                    localeAdapter.getList()[pos].translatedText =
                        it.outputData.getString("sentenceTranslated").toString()
                    Timber.d("workManagerSortie = %s",
                        it.outputData.getString("sentenceTranslated").toString())
                    localeAdapter.notifyDataSetChanged()
                }
            })
    }

    /**
     * Check permissions
     */
    private fun checkPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            recordAudioRequestCode
        )
    }

    /**
     * Request Permissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == recordAudioRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }
}