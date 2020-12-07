package fr.enssat.babelblock.delvoye_legal.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow


class TranslationBlockRepository(private val translationBlockDao: TranslationBlockDao) {
    // Create actions
    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(translationBlock: TranslationBlock) {
        translationBlockDao.insert(translationBlock)
    }

    // Read actions
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allTranslationBlocks: Flow<List<TranslationBlock>> =
        translationBlockDao.getAll()

    // Update actions
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(translationBlock: TranslationBlock) {
        translationBlockDao.update(translationBlock)
    }

    // Delete actions
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        translationBlockDao.deleteAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(translationBlock: TranslationBlock) {
        translationBlockDao.delete(translationBlock)
    }
}