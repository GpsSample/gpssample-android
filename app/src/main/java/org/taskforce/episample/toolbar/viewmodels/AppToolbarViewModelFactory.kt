package org.taskforce.episample.toolbar.viewmodels

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.core.language.LiveLanguageService

class AppToolbarViewModelFactory(private val application: Application,
                                 private val titleResId: Int,
                                 private val enumerationSubject: EnumerationSubject): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AppToolbarViewModel(titleResId,
                LiveLanguageService(application, enumerationSubject, listOf())) as T
    }
}