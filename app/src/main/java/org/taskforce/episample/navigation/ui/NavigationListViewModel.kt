package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.NavigationManager
import javax.inject.Inject

class NavigationListViewModel(application: Application): AndroidViewModel(application) {

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var navigationManager: NavigationManager
    
    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }
    
    val navigationPlans = navigationManager.getNavigationPlans()
}