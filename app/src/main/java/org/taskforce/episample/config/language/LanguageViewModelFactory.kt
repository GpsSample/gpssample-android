package org.taskforce.episample.config.name

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.language.LanguageViewModel
import org.taskforce.episample.toolbar.managers.LanguageManager

class LanguageViewModelFactory(private val languageManager: LanguageManager,
                               private val languageService: LanguageService,
                               private val stepper: Stepper,
                               private val configBuildManager: ConfigBuildManager,
                               private val errorCallback: LanguageViewModel.LanguageErrorCallback) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LanguageViewModel(languageManager,
                languageService,
                stepper,
                configBuildManager, 
                errorCallback) as T
    }

}