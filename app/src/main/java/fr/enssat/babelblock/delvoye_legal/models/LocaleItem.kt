package fr.enssat.babelblock.delvoye_legal.models

import androidx.work.WorkInfo.State
import java.util.*

data class LocaleItem(
        var locale: Locale,
        var translatedText: String
)