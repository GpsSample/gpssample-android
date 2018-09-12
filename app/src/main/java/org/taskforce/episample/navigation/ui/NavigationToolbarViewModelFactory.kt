package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class NavigationToolbarViewModelFactory(private val application: Application,
                                        private val titleResId: Int) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NavigationToolbarViewModel(application,
                titleResId) as T
    }
}
