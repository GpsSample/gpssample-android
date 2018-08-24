package org.taskforce.episample.config.settings.admin

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.language.LanguageService

class AdminSettingsViewModelFactory(private val stepper: Stepper,
                                    private val configBuildManager: ConfigBuildManager) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AdminSettingsViewModel(stepper,
                configBuildManager) as T
    }
}