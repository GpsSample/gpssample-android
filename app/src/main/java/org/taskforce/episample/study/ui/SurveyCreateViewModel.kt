package org.taskforce.episample.study.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.UserSession
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class SurveyCreateViewModel(application: Application,
                            private val dismiss: () -> Unit): AndroidViewModel(application) {

    @Inject
    lateinit var studyRepository: StudyRepository

    @Inject
    lateinit var userSession: UserSession

    @Inject
    lateinit var languageManager: LanguageManager
    val languageService: LanguageService

    init {
        (application as EpiApplication).collectComponent?.inject(this)

        languageService = LanguageService(languageManager)
    }

    val titleText = MutableLiveData<String>().apply { value = languageService.getString(R.string.sample_create_title) }

    val createSamplesButton = MutableLiveData<String>().apply { value = languageService.getString(R.string.sample_create_button) }

    fun generateSamples() {
        studyRepository.createDemoNavigationPlan(userSession.studyId) { _ ->  
            dismiss()
        }
    }
}
