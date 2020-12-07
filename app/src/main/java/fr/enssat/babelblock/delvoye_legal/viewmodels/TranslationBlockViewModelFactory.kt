package fr.enssat.babelblock.delvoye_legal.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.enssat.babelblock.delvoye_legal.database.TranslationBlockRepository
import fr.enssat.babelblock.delvoye_legal.TranslationBlockViewModel

class TranslationBlockViewModelFactory(private val repository: TranslationBlockRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranslationBlockViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TranslationBlockViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}