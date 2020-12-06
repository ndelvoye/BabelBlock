package fr.enssat.babelblock.delvoye_legal.Tools.impl

import android.os.Build
import android.speech.tts.TextToSpeech
import fr.enssat.babelblock.delvoye_legal.MainActivity
import fr.enssat.babelblock.delvoye_legal.Tools.TextToSpeechTool
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.util.*
import kotlin.coroutines.suspendCoroutine

class TextToSpeechHandler(private val locale: Locale?) : TextToSpeechTool {

    private var speaker: TextToSpeech? = null

    override suspend fun speak(context: MainActivity, text: String): Unit = suspendCoroutine {
        speaker = TextToSpeech(context) { status ->
            Timber.d("status: $status")
            speaker?.language = locale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Use non-deprecated method
                speaker?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                speaker?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
            }
        }
    }
}