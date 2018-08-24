package org.taskforce.episample.config.study

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.language.LanguageService

class StudyCreateViewModelFactory(private val application: Application,
                                  private val languageService: LanguageService,
                                  private val enabledColor: Int,
                                  private val disabledColor: Int,
                                  private val createStudy: (configId: String, studyId: String) -> Unit,
                                  private val configId: String,
                                  private val share: Boolean) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StudyCreateViewModel(application,
                languageService,
                enabledColor,
                disabledColor,
                createStudy,
                configId,
                share) as T
    }
}