package org.taskforce.episample.config.base

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.language.LanguageService

class ConfigSuccessViewModelFactory(private val application: Application,
                                    private val languageService: LanguageService,
                                    private val backToMain: () -> Unit,
                                    private val createNewStudy: (String) -> Unit,
                                    private val configManager: ConfigManager,
                                    private val config: Config,
                                    private val defaultColor: Int,
                                    private val successColor: Int,
                                    private val failureColor: Int): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ConfigSuccessViewModel(application,
                languageService,
                backToMain,
                createNewStudy,
                configManager,
                config,
                defaultColor,
                successColor,
                failureColor) as T
    }
}