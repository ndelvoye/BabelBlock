package fr.enssat.babelblock.delvoye_legal

import android.app.Application
import fr.enssat.babelblock.delvoye_legal.Database.TranslationBlockRepository
import fr.enssat.babelblock.delvoye_legal.Database.TranslationBlockRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TranslationBlocksApplication: Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { TranslationBlockRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TranslationBlockRepository(database.translationBlockDao()) }
}