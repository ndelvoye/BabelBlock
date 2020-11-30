package fr.enssat.babelblock.delvoye_legal.Tools.impl

import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import fr.enssat.babelblock.delvoye_legal.Tools.TranslationTool
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import timber.log.Timber
import java.util.*


class TranslatorHandler(from: Locale, to: Locale) : TranslationTool {
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(from.language)
        .setTargetLanguage(to.language)
        .build()

    private val translator = Translation.getClient(options)

    override fun translateAsync(text: String): Deferred<String> {
        Timber.d("Translation of $text from $options.zzb to $options.zza started")
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