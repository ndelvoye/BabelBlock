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
import androidx.work.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import fr.enssat.babelblock.delvoye_legal.models.LocaleItem
import fr.enssat.babelblock.delvoye_legal.tools.BlockService
import fr.enssat.babelblock.delvoye_legal.tools.SpeechToTextTool
import fr.enssat.babelblock.delvoye_legal.utils.LocaleUtils
import fr.enssat.babelblock.delvoye_legal.workers.TranslateWorker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.*
import kotlin.reflect.typeOf

class MainActivity : AppCompatActivity() {
    private val recordAudioRequestCode = 1

    private lateinit var speechToText: SpeechToTextTool
    private lateinit var selectedSpokenLanguage: Locale
    private lateinit var localeAdapter: LocaleRecyclerAdapter

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
                            CoroutineScope(Dispatchers.Main).launch {
                                sentencePronouncedSpinner.visibility = View.INVISIBLE
                                sentencePronouncedTitle.visibility = View.VISIBLE
                                sentencePronounced.visibility = View.VISIBLE
                                sentencePronounced.text = text.capitalize(selectedSpokenLanguage)
                                val localeItemList = localeAdapter.getList()
                                // build the flow
                                val localeItemFlow = flow {
                                    localeItemList.forEach { emit(it) }
                                }
                                // consume the flow (Sequentially)
                                var count = 0
                                localeItemFlow.map { localeItem ->
                                    val index = localeAdapter.getList().indexOf(localeItem)
                                    Timber.d("Started $localeItem (#$index)")
                                    var previousItem = LocaleItem(selectedSpokenLanguage, text)
                                    if (index != 0) previousItem = localeItemList[index - 1]
                                    service.translator(previousItem.locale, localeItem.locale)
                                        .translate(previousItem.translatedText).await()
                                }.onEach { res ->
                                    localeItemList[count].translatedText = res
                                    localeAdapter.notifyDataSetChanged()
                                    count++
                                }.collect {
                                    Timber.d("Translated $it")
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
                            ""
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
        workManager.beginUniqueWork(
            "TranslationJob",
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        ).enqueue()
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observe(this, {
                if (it.state.isFinished) {
                    localeAdapter.getList()[pos].translatedText =
                        it.outputData.getString("sentenceTranslated").toString()
                    Timber.d(
                        "workManagerSortie = %s",
                        it.outputData.getString("sentenceTranslated").toString()
                    )
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