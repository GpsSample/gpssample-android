package org.taskforce.episample.mapbox

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.res.Resources

class MapboxDownloadViewModelFactory(private val application: Application, private val resources: Resources) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapboxDownloadViewModel(
                application,
                resources) as T
    }
}