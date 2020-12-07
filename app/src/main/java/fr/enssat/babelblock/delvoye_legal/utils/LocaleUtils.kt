package fr.enssat.babelblock.delvoye_legal.utils

import fr.enssat.babelblock.delvoye_legal.R
import java.util.*

object LocaleUtils {
    @JvmStatic
    fun getAvailableLocales(): List<Locale> {
        return listOf(
                Locale.FRENCH,
                Locale.ENGLISH,
                Locale("es", "ES"),
                Locale.CHINESE,
                Locale.ITALIAN,
                Locale.JAPANESE,
                Locale.GERMAN
        )
    }
    @JvmStatic
    fun stringToLocale(s: String): Locale {
        return when (s) {
            "fr" -> Locale.FRENCH
            "en" -> Locale.ENGLISH
            "es" -> Locale("es", "ES")
            "zh" -> Locale.CHINESE
            "it" -> Locale.ITALIAN
            "ja" -> Locale.JAPANESE
            "de" -> Locale.GERMAN
            else -> throw Exception("Locale not recognized : $s")
        }
    }

    @JvmStatic
    fun stringToFlagInt(s: String): Int {
        return when (s) {
            "fr" -> R.mipmap.france_flag
            "en" -> R.mipmap.united_kingdom_flag
            "es" -> R.mipmap.spain_flag
            "zh" -> R.mipmap.china_flag
            "it" -> R.mipmap.italy_flag
            "ja" -> R.mipmap.japan_flag
            "de" -> R.mipmap.germany_flag
            else -> throw Exception("Language not recognized : $s")
        }
    }

    @JvmStatic
    fun reduceLanguage(s: String): String {
        return when (s) {
            "French" -> "fr"
            "English" -> "en"
            "Spanish" -> "es"
            "Chinese" -> "zh"
            "Italian" -> "it"
            "Japanese" -> "ja"
            "German" -> "de"
            else -> throw Exception("Locale not recognized : $s")
        }
    }
}