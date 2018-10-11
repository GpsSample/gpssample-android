package org.taskforce.episample.sampling.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.res.Resources
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.core.interfaces.EnumerationSubject

class SurveyCreateViewModelFactory(val resources: Resources, val enumerationSubject: EnumerationSubject, val collectManager: CollectManager, val displaySettings: DisplaySettings) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SamplingGenerationViewModel(resources, enumerationSubject, collectManager, displaySettings) as T
    }
}