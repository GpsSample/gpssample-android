package org.taskforce.episample.config.base

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.language.LanguageService

class ConfigUploadViewModelFactory(private val languageService: LanguageService,
                                    private val stepper: Stepper) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ConfigUploadViewModel(languageService, stepper) as T
    }

}