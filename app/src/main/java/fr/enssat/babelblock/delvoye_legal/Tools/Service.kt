package fr.enssat.babelblock.delvoye_legal.Tools

import fr.enssat.babelblock.delvoye_legal.MainActivity
import fr.enssat.babelblock.delvoye_legal.Tools.impl.SpeechRecognizerHandler
import fr.enssat.babelblock.delvoye_legal.Tools.impl.TextToSpeechHandler
import fr.enssat.babelblock.delvoye_legal.Tools.impl.TranslatorHandler
import kotlinx.coroutines.Deferred
import java.util.*

interface TextToSpeechTool {
    suspend fun speak(context: MainActivity, text: String)
}

interface TranslationTool {
    fun translateAsync(text: String): Deferred<String>
    fun close()
}

interface SpeechToTextTool {
    interface Listener {
        fun onResult(text: String, isFinal: Boolean)
        fun onError()
    }

    fun start(listener: Listener)
    fun stop()
    fun close()
}

class BlockService(private val context: MainActivity) {
    fun textToSpeech(from: Locale?): TextToSpeechTool {
        return TextToSpeechHandler(from)
    }

    fun translator(from: Locale, to: Locale): TranslationTool =
            TranslatorHandler(from, to)

    fun speechToText(from: Locale?): SpeechToTextTool =
            SpeechRecognizerHandler(context.applicationContext, from)
}

