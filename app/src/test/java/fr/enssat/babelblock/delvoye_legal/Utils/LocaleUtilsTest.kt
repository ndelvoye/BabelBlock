package fr.enssat.babelblock.delvoye_legal.Utils

import fr.enssat.babelblock.delvoye_legal.Utils.LocaleUtils
import fr.enssat.babelblock.delvoye_legal.Utils.LocaleUtils.getAvailableLocales
import fr.enssat.babelblock.delvoye_legal.Utils.LocaleUtils.reduceLanguage
import fr.enssat.babelblock.delvoye_legal.Utils.LocaleUtils.stringToFlagInt
import fr.enssat.babelblock.delvoye_legal.Utils.LocaleUtils.stringToLocale
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