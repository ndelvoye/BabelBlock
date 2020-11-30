package fr.enssat.babelblock.delvoye_legal.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TranslationBlock::class], version = 1)
abstract class TranslationBlockRoomDatabase : RoomDatabase() {

    abstract fun translationBlockDao(): TranslationBlockDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TranslationBlockRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TranslationBlockRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TranslationBlockRoomDatabase::class.java,
                    "translation_block_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(TranslationBlockDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        private class TranslationBlockDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.translationBlockDao())
                    }
                }
            }

            suspend fun populateDatabase(translationBlockDao: TranslationBlockDao) {
                // Delete all content when starting the app
                translationBlockDao.deleteAll()
            }
        }
    }
}