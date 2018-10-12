package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.core.interfaces.NavigationItem

class NavigationDetailsViewModelFactory(private val application: Application,
                                        private val navigationItem: NavigationItem,
                                        private val incompleteColor: Int,
                                        private val completeColor: Int,
                                        private val skippedColor: Int,
                                        private val problemColor: Int,
                                        private val changeStatus: (NavigationItem) -> Unit): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NavigationDetailsViewModel(application, 
                navigationItem,
                incompleteColor, 
                completeColor, 
                skippedColor, 
                problemColor,
                changeStatus) as T
    }
}