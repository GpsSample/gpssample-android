package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.LocationService
import org.taskforce.episample.core.interfaces.NavigationItem
import org.taskforce.episample.core.interfaces.NavigationManager
import org.taskforce.episample.core.language.LanguageService
import org.taskforce.episample.core.navigation.SurveyStatus
import javax.inject.Inject

class NavigationViewModel(application: Application, navigationPlanId: String): AndroidViewModel(application) {

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var languageService: LanguageService

    @Inject
    lateinit var locationService: LocationService

    var launchedSurvey: Boolean = false

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }

    val nextNavigationItem: LiveData<NavigationItem?> = Transformations.map(navigationManager.getNavigationItems(navigationPlanId)) {
        return@map it.sortedBy { it.navigationOrder }.firstOrNull { it.surveyStatus is SurveyStatus.Incomplete }
    }
    
    val landmarks = navigationManager.getLandmarks()
    
    val navigationItems = navigationManager.getNavigationItems(navigationPlanId)
    
    val breadcrumbs = navigationManager.getBreadcrumbs()
}
