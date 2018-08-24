package org.taskforce.episample.config.settings.server

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.widget.ArrayAdapter
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.language.LanguageService

class ServerSettingsViewModelFactory(private val languageService: LanguageService,
                                     private val stepper: Stepper,
                                     private val configBuildManager: ConfigBuildManager,
                                     private val serverTypeAdapter: ArrayAdapter<String>,
                                     private val serverSelectPositionProvider: () -> Int?,
                                     private val serverOptions: Array<String>) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ServerSettingsViewModel(languageService,
                stepper,
                configBuildManager,
                serverTypeAdapter,
                serverSelectPositionProvider,
                serverOptions) as T
    }
}