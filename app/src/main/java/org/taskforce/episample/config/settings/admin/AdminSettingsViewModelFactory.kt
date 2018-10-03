package org.taskforce.episample.config.settings.admin

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.base.ConfigBuildManager

class AdminSettingsViewModelFactory(private val configBuildManager: ConfigBuildManager) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AdminSettingsViewModel(configBuildManager) as T
    }
}