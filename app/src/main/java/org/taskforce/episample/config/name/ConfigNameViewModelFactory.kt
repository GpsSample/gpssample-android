package org.taskforce.episample.config.name

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper

class ConfigNameViewModelFactory(private val application: Application,
                                 private val stepper: Stepper,
                                 private val configBuildManager: ConfigBuildManager) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ConfigNameViewModel(application,
                stepper,
                configBuildManager) as T
    }

}