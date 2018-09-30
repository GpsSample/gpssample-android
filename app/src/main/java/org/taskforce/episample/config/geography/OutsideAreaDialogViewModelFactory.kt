package org.taskforce.episample.config.geography

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.language.LanguageService

class OutsideAreaDialogViewModelFactory(private val languageService: LanguageService,
                                        private val continueSave: () -> Unit,
                                        private val goBack: () -> Unit): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return OutsideAreaDialogViewModel(languageService,
                continueSave, 
                goBack) as T
    }
}