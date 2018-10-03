package org.taskforce.episample.config.geography

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.language.LanguageService

class GeographyViewModelFactory(private val languageService: LanguageService,
                                private val adapter: EnumerationAreaAdapter,
                                private val configBuildManager: ConfigBuildManager) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GeographyViewModel(languageService,
                adapter,
                configBuildManager) as T
    }
}