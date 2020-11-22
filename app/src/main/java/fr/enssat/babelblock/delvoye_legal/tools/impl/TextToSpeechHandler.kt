package fr.enssat.babelblock.delvoye_legal.tools.impl

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import fr.enssat.babelblock.delvoye_legal.tools.TextToSpeechTool
import timber.log.Timber
import java.util.*

class TextToSpeechHandler(context: Context, private val locale: Locale?) : TextToSpeechTool {

    private val speaker = TextToSpeech(context) { status -> Timber.d("status: $status") }

    override fun speak(text: String) {
        speaker.language = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Use non-deprecated method
            speaker.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            speaker.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    override fun stop() {
        speaker.stop()
    }

    override fun close() {
        speaker.shutdown()
    }
}