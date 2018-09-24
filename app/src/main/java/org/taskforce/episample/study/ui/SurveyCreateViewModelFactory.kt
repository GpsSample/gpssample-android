package org.taskforce.episample.study.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class SurveyCreateViewModelFactory(private val application: Application,
                                   private val dismiss: () -> Unit) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SurveyCreateViewModel(application,
                dismiss) as T
    }
}