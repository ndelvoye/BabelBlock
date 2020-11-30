package fr.enssat.babelblock.delvoye_legal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.enssat.babelblock.delvoye_legal.Database.TranslationBlock
import fr.enssat.babelblock.delvoye_legal.Database.TranslationBlockRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class TranslationBlockViewModel(private val repository: TranslationBlockRepository) : ViewModel() {
    // Create actions
    // Launch in this scope = Launching a new coroutine to insert the data in a non-blocking way
    fun insert(translationBlock: TranslationBlock) = viewModelScope.launch {
        Timber.d("Inserting $translationBlock")
        repository.insert(translationBlock)
    }

    // Read actions
    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allTranslationBlocks: LiveData<List<TranslationBlock>> =
        repository.allTranslationBlocks.asLiveData()

    // Update actions
    fun update(translationBlock: TranslationBlock) = viewModelScope.launch {
        Timber.d("Updating $translationBlock")
        repository.update(translationBlock)
    }

    // Delete actions
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun delete(translationBlock: TranslationBlock) = viewModelScope.launch {
        Timber.d("Deleting $translationBlock")
        repository.delete(translationBlock)
    }
}

