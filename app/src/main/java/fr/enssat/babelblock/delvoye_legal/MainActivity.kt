package fr.enssat.babelblock.delvoye_legal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import fr.enssat.babelblock.delvoye_legal.adapter.TranslationBlocksAdapter
import fr.enssat.babelblock.delvoye_legal.database.TranslationBlock
import fr.enssat.babelblock.delvoye_legal.tools.BlockService
import fr.enssat.babelblock.delvoye_legal.tools.SpeechToTextTool
import fr.enssat.babelblock.delvoye_legal.tools.TextToSpeechTool
import fr.enssat.babelblock.delvoye_legal.utils.LocaleUtils
import fr.enssat.babelblock.delvoye_legal.viewmodels.TranslationBlockViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_locale_list_item.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*


class MainActivity : AppCompatActivity() {
    private val recordAudioRequestCode = 1

    private lateinit var speechToText: SpeechToTextTool
    private lateinit var textToSpeech: TextToSpeechTool
    private lateinit var selectedSpokenLanguage: String
    private var translationBlocksCounter: Int = 0
    private var translationBlocksMaxPos: Int = 0

    private val translationBlockViewModel: TranslationBlockViewModel by viewModels {
        TranslationBlockViewModelFactory((application as TranslationBlocksApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }

        // LOGS
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        // TOOLBAR
        setSupportActionBar(myToolbar)
        myToolbar.title = R.string.app_name.toString()

        // Downloading every models
        var count = 0
        val modelManager = RemoteModelManager.getInstance()
        modelManager
                .getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener { models ->
                    LocaleUtils.getAvailableLocales().forEach {
                        if (!models.contains(TranslateRemoteModel.Builder(it.language).build())) {
                            modelManager.download(
                                    TranslateRemoteModel.Builder(it.language).build(),
                                    DownloadConditions.Builder()
                                            .requireWifi()
                                            .build()
                            ).addOnSuccessListener {
                                count++
                            }
                        } else {
                            count++
                        }
                    }
                }
                .addOnFailureListener {
                    // Error.
                }

        // RECYCLER VIEW & ADAPTER
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val adapter = TranslationBlocksAdapter(translationBlockViewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Add an observer on the LiveData returned by getTranslationBlocks
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        translationBlockViewModel.allTranslationBlocks.observe(this) { translationBlocks ->
            translationBlocks.let {
                adapter.submitList(it)
            }
            translationBlocksCounter = translationBlocks.size
            translationBlocks.forEach {
                if (it.position > translationBlocksMaxPos) translationBlocksMaxPos = it.position
            }
        }

        // Drag & drop + Delete by Swiping (UI)
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                if (from != -1 && to != -1) {
                    val item1 = adapter.currentList[from]
                    val item2 = adapter.currentList[to]
                    if (item1 != null && item2 != null) {
                        val newItem2 = item2.copy()
                        item2.position = item1.position
                        item1.position = newItem2.position
                        translationBlockViewModel.update(item1)
                        translationBlockViewModel.update(item2)
                        adapter.notifyDataSetChanged()
                        return true
                    }
                }
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                translationBlockViewModel.delete(adapter.currentList[viewHolder.adapterPosition])
                Toast.makeText(this@MainActivity, "Block deleted", Toast.LENGTH_SHORT).show()
            }
        }).attachToRecyclerView(recyclerView)

        // Init BlockService language to French (first item of Spinner)
        val service = BlockService(this)
        selectedSpokenLanguage = "fr"
        speechToText = service.speechToText(LocaleUtils.stringToLocale(selectedSpokenLanguage))
        textToSpeech = service.textToSpeech(LocaleUtils.stringToLocale(selectedSpokenLanguage))

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
                                LocaleUtils.reduceLanguage(selectSpokenLanguageSpinner.selectedItem.toString())
                        speechToText =
                                service.speechToText(
                                        LocaleUtils.stringToLocale(
                                                selectedSpokenLanguage
                                        )
                                )
                        textToSpeech = service.textToSpeech(LocaleUtils.stringToLocale(
                                selectedSpokenLanguage
                        ))
                        Timber.d("selectedSpokenLanguage = $selectedSpokenLanguage")
                    }
                }

        startTalkButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                pressTheButton.visibility = View.INVISIBLE // Hide "Press the microphone button ..."
                pressTheTextButton.visibility = View.INVISIBLE
                sentencePronouncedTitle.visibility = View.INVISIBLE // Hide "You said :"
                sentencePronounced.text = "" // Delete old SpeechToText
                sentencePronouncedSpinner.visibility = View.VISIBLE // Display Loading Spinner
                sentencePronouncedListenButton.visibility = View.INVISIBLE

                v.performClick()
                Snackbar.make(startTalkButton, "Listening...", Snackbar.LENGTH_SHORT).show()
                speechToText.start(object : SpeechToTextTool.Listener {
                    override fun onResult(text: String, isFinal: Boolean) {
                        if (isFinal) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sentencePronouncedSpinner.visibility = View.INVISIBLE
                                sentencePronouncedTitle.visibility = View.VISIBLE
                                sentencePronounced.visibility = View.VISIBLE
                                sentencePronouncedListenButton.visibility = View.VISIBLE
                                sentencePronounced.text = text.capitalize(
                                        LocaleUtils.stringToLocale(selectedSpokenLanguage)
                                )

                                // EDIT LE TTS
                                sentencePronouncedListenButton.setOnClickListener {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        textToSpeech.speak(this@MainActivity, text)
                                    }
                                }

                                Timber.d("=> ${adapter.currentList}")
                                val localeItemFlow = flow {
                                    adapter.currentList.forEach {
                                        emit(it)
                                    }
                                }
                                // consume the flow (Sequentially)
                                var itemCounter = 0
                                localeItemFlow.map { translationBlock ->
                                    val index = adapter.currentList.indexOf(translationBlock)
                                    Timber.d("Started $translationBlock (#$index)")
                                    if (index == 0) {
                                        service.translator(
                                                LocaleUtils.stringToLocale(selectedSpokenLanguage),
                                                LocaleUtils.stringToLocale(translationBlock.language)
                                        ).translateAsync(text).await()
                                    } else {
                                        val previousItem = adapter.currentList[index - 1]
                                        service.translator(
                                                LocaleUtils.stringToLocale(previousItem.language),
                                                LocaleUtils.stringToLocale(translationBlock.language)
                                        ).translateAsync(previousItem.translation).await()
                                    }
                                }.onEach { res ->
                                    Timber.d("Updating item #$itemCounter")
                                    val currentItem = adapter.currentList[itemCounter]
                                    currentItem.translation = res.capitalize(
                                            LocaleUtils.stringToLocale(currentItem.language)
                                    )
                                    translationBlockViewModel.update(currentItem)
                                    itemCounter++
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
                        pressTheTextButton.visibility = View.VISIBLE
                    }
                })
            }
            false
        }


        startWriteButton.setOnClickListener {
            Timber.d("Opened Write Button dialog")
            pressTheButton.visibility = View.INVISIBLE // Hide "Press the microphone button ..."
            pressTheTextButton.visibility = View.INVISIBLE
            sentencePronouncedTitle.visibility = View.INVISIBLE // Hide "You said :"
            sentencePronounced.text = "" // Delete old SpeechToText
            sentencePronouncedSpinner.visibility = View.VISIBLE // Display Loading Spinner
            sentencePronouncedListenButton.visibility = View.INVISIBLE
            val dialog: MaterialDialog =MaterialDialog(this).title(R.string.write_dialog_title).show {
                input(){dialog, text ->
                    // Text submitted with the action button
                    CoroutineScope(Dispatchers.Main).launch {
                        var textScope = ""+ text.toString()

                        sentencePronounced.text = textScope.capitalize(
                                LocaleUtils.stringToLocale(selectedSpokenLanguage)
                        )
                        sentencePronouncedSpinner.visibility = View.INVISIBLE
                        sentencePronouncedTitle.visibility = View.VISIBLE
                        sentencePronounced.visibility = View.VISIBLE
                        sentencePronouncedListenButton.visibility = View.VISIBLE

                        // EDIT LE TTS
                        /*
                        sentencePronouncedListenButton.setOnClickListener {
                            CoroutineScope(Dispatchers.Default).launch {
                                textToSpeech.speak(this@MainActivity, textScope)
                            }
                        }*/

                        Timber.d("=> ${adapter.currentList}")
                        val localeItemFlow = flow {
                            adapter.currentList.forEach {
                                emit(it)
                            }
                        }
                        // consume the flow (Sequentially)
                        var itemCounter = 0
                        localeItemFlow.map { translationBlock ->
                            val index = adapter.currentList.indexOf(translationBlock)
                            Timber.d("Started $translationBlock (#$index)")
                            if (index == 0) {
                                service.translator(
                                        LocaleUtils.stringToLocale(selectedSpokenLanguage),
                                        LocaleUtils.stringToLocale(translationBlock.language)
                                ).translateAsync(textScope).await()
                            } else {
                                val previousItem = adapter.currentList[index - 1]
                                service.translator(
                                        LocaleUtils.stringToLocale(previousItem.language),
                                        LocaleUtils.stringToLocale(translationBlock.language)
                                ).translateAsync(previousItem.translation).await()
                            }
                        }.onEach { res ->
                            Timber.d("Updating item #$itemCounter")
                            val currentItem = adapter.currentList[itemCounter]
                            currentItem.translation = res.capitalize(
                                    LocaleUtils.stringToLocale(currentItem.language)
                            )
                            translationBlockViewModel.update(currentItem)
                            itemCounter++
                        }.collect {
                            Timber.d("Translated $it")
                        }
                    }
                }
                positiveButton(R.string.submit)
            }


        }


        addLanguageButton.setOnClickListener {
            Timber.d("Opened AddTranslationBlockDialog")
            MaterialDialog(this).title(R.string.select_language_to_translate_dialog_title).show {
                listItemsSingleChoice(R.array.languages) { _, _, selectedLanguage ->
                    Timber.d("Selected language '$selectedLanguage'")
                    var index = 0
                    if (translationBlocksCounter != 0) index = translationBlocksMaxPos + 1
                    translationBlockViewModel.insert(
                            TranslationBlock(
                                    index,
                                    LocaleUtils.reduceLanguage(selectedLanguage as String),
                                    ""
                            )
                    )
                }
            }
        }


    }

    override fun onDestroy() {
        speechToText.close()
        super.onDestroy()
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