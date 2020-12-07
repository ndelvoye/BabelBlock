package fr.enssat.babelblock.delvoye_legal.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface TranslationBlockDao {
    // Create action
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(translationBlock: TranslationBlock)

    // Read actions
    @Query("SELECT * FROM TranslationBlock")
    fun getAll(): Flow<List<TranslationBlock>>

    @Query("SELECT * FROM TranslationBlock WHERE position = :position")
    fun findByPosition(position: Int): TranslationBlock

    // Update actions
    @Update
    suspend fun update(translationBlock: TranslationBlock)

    // Delete actions
    @Query("DELETE FROM TranslationBlock")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(translationBlock: TranslationBlock)
}