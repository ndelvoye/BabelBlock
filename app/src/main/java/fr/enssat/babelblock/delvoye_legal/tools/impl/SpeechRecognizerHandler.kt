package fr.enssat.babelblock.delvoye_legal.tools.impl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import fr.enssat.babelblock.delvoye_legal.tools.SpeechToTextTool
import java.util.*

class SpeechRecognizerHandler(context: Context, locale: Locale?) : SpeechToTextTool {

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context).not()) {
            Log.e("Reco", "Sorry but Speech recognizer is not available on this device")
            throw IllegalStateException()
        }
    }

    private var listener: SpeechToTextTool.Listener? = null

    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("Reco", "ready $params")
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Log.d("Reco", "Error : $error")
            }

            override fun onBeginningOfSpeech() {
                Log.d("Reco", "Start Listening")
                listener?.onResult("Start Listening...", false)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("Reco", "partial : $partialResults")
                partialResults?.getResult()?.apply {
                    Log.d("Reco", "partial : $this")
                    listener?.onResult(this, false)
                }
            }

            override fun onResults(results: Bundle?) {
                Log.d("Reco", "result : $results")
                results?.getResult()?.apply {
                    Log.d("Reco", "result : $this")
                    listener?.onResult(this, true)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toString())
        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, locale.toString())
    }

    override fun start(listener: SpeechToTextTool.Listener) {
        this.listener = listener
        speechRecognizer.startListening(intent)
    }

    override fun stop() {
        speechRecognizer.stopListening()
        speechRecognizer.cancel()
    }

    override fun close() {
        speechRecognizer.destroy()
    }

    private fun Bundle.getResult(): String? =
        this.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)

}