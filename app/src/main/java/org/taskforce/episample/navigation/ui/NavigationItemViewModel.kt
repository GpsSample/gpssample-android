package org.taskforce.episample.navigation.ui

import android.arch.lifecycle.LiveData
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.NavigationItem
import org.taskforce.episample.core.navigation.SurveyStatus

class NavigationItemViewModel(position: Int,
                              private val surveyStatus: SurveyStatus,
                              val title: String?,
                              val distance: LiveData<String>,
                              val data: NavigationItem,
                              val showDetails: (NavigationItem) -> Unit) {

    val positionVisibility = surveyStatus is SurveyStatus.Incomplete
    val surveyStatusImage: Int
        get() {
            return when (surveyStatus) {
                is SurveyStatus.Incomplete -> R.drawable.gray_circle
                is SurveyStatus.Complete -> R.drawable.icon_survey_complete
                is SurveyStatus.Problem -> R.drawable.icon_survey_problem
                is SurveyStatus.Skipped -> R.drawable.icon_survey_skipped
            }
    }

    val numericalOrder = (position + 1).toString()
    
    fun handleDetailsClick() {
        showDetails(data)
    }
}