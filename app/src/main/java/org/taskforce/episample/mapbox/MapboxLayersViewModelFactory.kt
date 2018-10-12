package org.taskforce.episample.mapbox

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class MapboxLayersViewModelFactory(val application: Application): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapboxLayersViewModel(application) as T
    }
}