package fr.enssat.babelblock.delvoye_legal.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "TranslationBlock")
data class TranslationBlock(
        @PrimaryKey
        @NotNull
        @ColumnInfo(name = "position")
        var position: Int,

        @NotNull
        @ColumnInfo(name = "language")
        var language: String,

        @ColumnInfo(name = "translation")
        var translation: String
)