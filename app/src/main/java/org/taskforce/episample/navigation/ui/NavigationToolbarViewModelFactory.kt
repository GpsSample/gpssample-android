package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class NavigationToolbarViewModelFactory(private val application: Application,
                                        private val titleResId: Int,
                                        private val titleSubject: String? = "") : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NavigationToolbarViewModel(application,
                titleResId,
                titleSubject) as T
    }
}
