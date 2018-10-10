package org.taskforce.episample.config.mapbox

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.base.ConfigBuildManager

class MapboxConfigViewModelFactory(private val configBuildManager: ConfigBuildManager) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapboxConfigViewModel(configBuildManager) as T
    }
}