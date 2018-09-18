package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.view.View
import android.widget.Toast
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.LiveDataTriple
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.LocationService
import org.taskforce.episample.core.interfaces.NavigationItem
import org.taskforce.episample.core.interfaces.NavigationManager
import org.taskforce.episample.core.language.LanguageService
import org.taskforce.episample.core.navigation.SurveyStatus
import javax.inject.Inject

class NavigationPlanViewModel(application: Application,
                              val startRoute: () -> Unit,
                              val addLandmark: () -> Unit) : AndroidViewModel(application) {

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var languageService: LanguageService

    @Inject
    lateinit var locationService: LocationService

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }

    override fun onCleared() {
        super.onCleared()
        languageService.cleanup()
    }

    val collectItems = navigationManager.getCollectItems()
    val possiblePath = navigationManager.getPossiblePath()
    val breadcrumbs = navigationManager.getBreadcrumbs()

    val navigationItems: LiveData<List<NavigationItem>> = navigationManager.getNavigationItems()

    private val isComplete: LiveData<Boolean> = Transformations.map(navigationItems) {
        it.none { it.surveyStatus is SurveyStatus.Incomplete}
    }

    val goToSyncVisibility: LiveData<Boolean> = Transformations.map(isComplete) {
        it
    }

    val startRouteVisibility: LiveData<Boolean> = Transformations.map(goToSyncVisibility) {
        !it
    }

    val goToSyncText: LiveData<String> = Transformations.map(languageService.getString(R.string.go_to_sync)) {
        return@map it.toUpperCase()
    }

    val startRouteText: LiveData<String> = Transformations.map(languageService.getString(R.string.start_route)) {
        return@map it.toUpperCase()
    }

    val landmarkButtonText: LiveData<String> = Transformations.map(languageService.getString(R.string.collect_button_landmark)) {
        return@map it.toUpperCase()
    }

    val navigationTitle: LiveData<String> = Transformations.switchMap(navigationItems, { navigationItems ->
        val incompleteCount = navigationItems.filter { it.surveyStatus is SurveyStatus.Incomplete }.size
        return@switchMap when (incompleteCount) {
            navigationItems.size -> languageService.getQuantityString(R.string.navigation_items_to_survey_var, incompleteCount)
            0 -> languageService.getString(R.string.assignment_complete)
            else -> languageService.getQuantityString(R.string.navigation_items_remaining_var, incompleteCount)
        }
    })

    private val skippedText: LiveData<String?> = Transformations.switchMap(navigationItems, { navigationItems ->
        val skippedCount = navigationItems.filter { it.surveyStatus is SurveyStatus.Skipped }.size
        return@switchMap if (skippedCount > 0) {
            languageService.getQuantityString(R.string.navigation_skipped_count_var, skippedCount)
        } else {
            MutableLiveData<String>().apply { value = null }
        }
    })

    private val problemText: LiveData<String?> = Transformations.switchMap(navigationItems, { navigationItems ->
        val problemCount = navigationItems.filter { it.surveyStatus is SurveyStatus.Problem }.size
        return@switchMap if (problemCount > 0) {
            languageService.getQuantityString(R.string.navigation_problem_count_var, problemCount)
        } else {
            MutableLiveData<String>().apply { value = null }
        }
    })

    val navigationDetailsDistance: LiveData<String> = Transformations.map(
            LiveDataPair(navigationManager.getPossiblePath(), navigationManager.getBreadcrumbs())
    ) {
        return@map "TODO distance remaining"
    }

    val completeDetailsInput: LiveData<String> = Transformations.switchMap(navigationItems) {
        return@switchMap languageService.getQuantityString(R.string.navigation_items_surveyed_var, it.size)
    }
    val incompleteNavigationDetailsInput: LiveDataTriple<String?, String?, String> = LiveDataTriple(skippedText, problemText, navigationDetailsDistance)
    val navigationDetailsInput = LiveDataTriple(completeDetailsInput, incompleteNavigationDetailsInput, isComplete)
    val navigationDetails: LiveData<String> = Transformations.map(navigationDetailsInput, {
        val completeText = it.first
        val incompleteDetails = it.second
        val skippedText = incompleteDetails.first
        val problemText = incompleteDetails.second
        val distanceText = incompleteDetails.third
        val isComplete = it.third

        return@map if (isComplete) {
            completeText
        } else {
            listOfNotNull(skippedText, problemText, distanceText).joinToString(separator = ", ")
        }
    })

    val nextNavigationItem = Transformations.map(navigationItems, {
        it.sortedBy { it.dateCreated }.firstOrNull { it.isIncomplete }
    })
    val startRouteEnabled = (Transformations.map(nextNavigationItem) {
        it != null
    } as MutableLiveData<Boolean>).apply { value = false }

    fun startRoute(view: View) {
        startRoute()
    }

    fun goToSync(view: View) {
        Toast.makeText(getApplication(), "Go to sync TODO", Toast.LENGTH_SHORT).show()
    }

    fun addLandmark(view: View) {
        addLandmark()
    }
}