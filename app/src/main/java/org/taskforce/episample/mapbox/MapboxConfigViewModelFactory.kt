package org.taskforce.episample.mapbox

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.res.Resources
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import org.taskforce.episample.config.base.ConfigBuildManager

class MapboxConfigViewModelFactory(private val latLngBounds: LatLngBounds, private val resources: Resources, private val configBuildManager: ConfigBuildManager) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapboxConfigViewModel(
                latLngBounds,
                resources,
                configBuildManager) as T
    }
}