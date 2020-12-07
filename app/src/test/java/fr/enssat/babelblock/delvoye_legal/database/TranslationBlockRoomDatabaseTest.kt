package fr.enssat.babelblock.delvoye_legal.database

import android.app.Application
import junit.framework.TestCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TranslationBlockRoomDatabaseTest : TestCase() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    fun testTranslationBlockDao() {}

    fun testGetDatabase() {
        assertNotNull(TranslationBlockRoomDatabase.getDatabase(Application(), applicationScope))
    }
}