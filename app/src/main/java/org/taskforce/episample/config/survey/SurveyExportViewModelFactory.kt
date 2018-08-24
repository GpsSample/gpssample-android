package org.taskforce.episample.config.survey

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.language.LanguageService

class SurveyExportViewModelFactory(private val languageService: LanguageService,
                                   private val configBuildManager: ConfigBuildManager): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SurveyExportViewModel(languageService,
                configBuildManager) as T
    }
}