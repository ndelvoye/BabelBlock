package fr.enssat.babelblock.delvoye_legal.tools.impl

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import fr.enssat.babelblock.delvoye_legal.tools.TranslationTool
import timber.log.Timber
import java.util.*

class TranslatorHandler(context: Context, from: Locale, to: Locale) : TranslationTool {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(from.language)
        .setTargetLanguage(to.language)
        .build()

    private val translator = Translation.getClient(options)

    private val conditions = DownloadConditions.Builder()
        .requireWifi()
        .build()

    init {
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { Timber.d("Model download completed") }
            .addOnFailureListener { e -> Timber.e(e, "Model download failed") }
    }

    override fun translate(text: String, callback: (String) -> Unit) {
        translator.translate(text)
            .addOnSuccessListener(callback)
            .addOnFailureListener { e -> Timber.e(e, "Translation failed") }
    }

    override fun close() {
        translator.close()
    }
}