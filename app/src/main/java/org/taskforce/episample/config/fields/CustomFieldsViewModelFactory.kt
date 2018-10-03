package org.taskforce.episample.config.fields

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.language.LanguageService

class CustomFieldsViewModelFactory(private val languageService: LanguageService,
                                   private val createNewField: () -> Unit,
                                   private val configBuildManager: ConfigBuildManager) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CustomFieldsViewModel(languageService,
                createNewField,
                configBuildManager) as T
    }
}