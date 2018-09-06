package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class NavigationViewModelFactory(private val application: Application,
                                 private val addLandmark: () -> Unit) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NavigationViewModel(application,
                addLandmark) as T
    }
}