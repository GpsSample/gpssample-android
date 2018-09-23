package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class NavigationPlanViewModelFactory(private val application: Application,
                                     private val navigationPlanId: String,
                                     private val startRoute: () -> Unit,
                                     private val addLandmark: () -> Unit) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NavigationPlanViewModel(application,
                navigationPlanId,
                startRoute,
                addLandmark) as T
    }
}