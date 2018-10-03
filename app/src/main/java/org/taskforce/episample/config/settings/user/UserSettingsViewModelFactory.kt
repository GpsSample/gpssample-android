package org.taskforce.episample.config.settings.user

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.widget.ArrayAdapter
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.language.LanguageService

class UserSettingsViewModelFactory(private val photoCompressionAdapter: ArrayAdapter<String>,
                                   private val photoCompressionSelection: () -> Int,
                                   private val configBuildManager: ConfigBuildManager) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UserSettingsViewModel(
                photoCompressionAdapter,
                photoCompressionSelection,
                configBuildManager) as T
    }
}