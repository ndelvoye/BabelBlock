package fr.enssat.babelblock.delvoye_legal.utils

import fr.enssat.babelblock.delvoye_legal.utils.LocaleUtils.getAvailableLocales
import fr.enssat.babelblock.delvoye_legal.utils.LocaleUtils.reduceLanguage
import fr.enssat.babelblock.delvoye_legal.utils.LocaleUtils.stringToFlagInt
import fr.enssat.babelblock.delvoye_legal.utils.LocaleUtils.stringToLocale
import junit.framework.TestCase

class LocaleUtilsTest : TestCase() {

    fun testGetAvailableLocales() {
        assertNotNull(getAvailableLocales())
    }

    fun testStringToLocale() {
        assertNotNull(stringToLocale("fr"))
    }

    fun testStringToFlagInt() {
        assertNotNull(stringToFlagInt("fr"))
    }

    fun testReduceLanguage() {
        assertNotNull(reduceLanguage("French"))
    }
}