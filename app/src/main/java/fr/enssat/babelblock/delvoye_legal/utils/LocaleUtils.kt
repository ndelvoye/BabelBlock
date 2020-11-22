package fr.enssat.babelblock.delvoye_legal.utils

import java.util.*

object LocaleUtils {
    @JvmStatic
    fun stringToLocale(s: String): Locale {
        return when (s) {
            "French" -> Locale.FRENCH
            "English" -> Locale.ENGLISH
            "Spanish" -> Locale("es", "ES")
            "Chinese" -> Locale.CHINESE
            "Italian" -> Locale.ITALIAN
            "Japanese" -> Locale.JAPANESE
            "German" -> Locale.GERMAN
            else -> throw Exception("Locale not recognized : $s")
        }
    }
}