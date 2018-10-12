package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.language.LanguageService

class DeleteItemDialogViewModelFactory(private val languageService: LanguageService,
                                       private val delete: () -> Unit,
                                       private val goBack: () -> Unit,
                                       private val subject: String): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DeleteItemDialogViewModel(languageService, 
                delete, 
                goBack,
                subject) as T
    }
}