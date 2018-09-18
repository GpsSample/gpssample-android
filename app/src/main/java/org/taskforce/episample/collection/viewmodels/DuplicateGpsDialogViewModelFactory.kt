package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.language.LanguageService

class DuplicateGpsDialogViewModelFactory(private val languageService: LanguageService,
                                         private val enumerationName: String,
                                         private val enumerationSubject: String,
                                         private val latitude: Double,
                                         private val longitude: Double,
                                         private val onCancel: () -> Unit,
                                         private val onDone: () -> Unit): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DuplicateGpsDialogViewModel(languageService, 
                enumerationName, 
                enumerationSubject, 
                latitude, 
                longitude,
                onCancel, 
                onDone) as T
    }
}