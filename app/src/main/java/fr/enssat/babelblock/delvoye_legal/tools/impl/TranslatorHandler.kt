package fr.enssat.babelblock.delvoye_legal.tools.impl

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import fr.enssat.babelblock.delvoye_legal.tools.TranslationTool
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
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

    override suspend fun translate(text: String): Deferred<String> {
        val deferred = CompletableDeferred<String>()

        translator.translate(text)
            .addOnSuccessListener { result -> deferred.complete(result) }
            .addOnFailureListener { exception -> deferred.completeExceptionally(exception) }

        return deferred
    }

    override fun close() {
        translator.close()
    }
}