package org.taskforce.episample.config.settings.display

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.widget.ArrayAdapter
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.language.LanguageService

class DisplaySettingsViewModelFactory(private val languageService: LanguageService,
                                      private val defaultLanguageAdapter: ArrayAdapter<String>,
                                      private val dateFormatAdapter: ArrayAdapter<String>,
                                      private val timeFormatAdapter: ArrayAdapter<String>,
                                      private val configBuildManager: ConfigBuildManager) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DisplaySettingsViewModel(languageService,
                defaultLanguageAdapter,
                dateFormatAdapter,
                timeFormatAdapter,
                configBuildManager) as T
    }
}