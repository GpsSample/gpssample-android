package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.view.View
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.NavigationManager
import org.taskforce.episample.core.navigation.SurveyStatus
import javax.inject.Inject

class SurveyStatusViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var navigationManager: NavigationManager

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }

    val title = MutableLiveData<String>().apply {
        value = application.getString(R.string.survey_status_title_var, config.enumerationSubject.singular)
    }
    val okButtonText: String = application.getString(R.string.submit)

    val cancelButtonText: String = application.getString(R.string.go_back)

    val surveyStatus = MutableLiveData<SurveyStatus>().apply { value = SurveyStatus.Incomplete() }
    val submitEnabled: LiveData<Boolean> = Transformations.map(surveyStatus, {
        it !is SurveyStatus.Incomplete
    })

    val submitTextColor: LiveData<Int> = Transformations.map(submitEnabled, {
        return@map if (it) {
            ContextCompat.getColor(application, R.color.colorAccent)
        } else {
            ContextCompat.getColor(application, R.color.textColorDisabled)
        }
    })

    val completeImage: LiveData<Int> = Transformations.map(surveyStatus) {
        return@map if (it is SurveyStatus.Complete) {
            R.drawable.icon_survey_complete
        } else {
            R.drawable.icon_survey_complete_unset
        }
    }

    val skippedImage: LiveData<Int> = Transformations.map(surveyStatus) {
        return@map if (it is SurveyStatus.Skipped) {
            R.drawable.icon_survey_skipped
        } else {
            R.drawable.icon_survey_skipped_unset
        }
    }

    val problemImage: LiveData<Int> = Transformations.map(surveyStatus) {
        return@map if (it is SurveyStatus.Problem) {
            R.drawable.icon_survey_problem
        } else {
            R.drawable.icon_survey_problem_unset
        }
    }

    val completeTextStyle = Transformations.map(surveyStatus) {
        return@map if (it is SurveyStatus.Complete) {
            Typeface.BOLD
        } else {
            Typeface.NORMAL
        }
    }

    val skippedTextStyle = Transformations.map(surveyStatus) {
        return@map if (it is SurveyStatus.Skipped) {
            Typeface.BOLD
        } else {
            Typeface.NORMAL
        }
    }

    val problemTextStyle = Transformations.map(surveyStatus) {
        return@map if (it is SurveyStatus.Problem) {
            Typeface.BOLD
        } else {
            Typeface.NORMAL
        }
    }


    fun completeImageViewClicked(view: View) {
        surveyStatus.postValue(SurveyStatus.Complete())
    }
}