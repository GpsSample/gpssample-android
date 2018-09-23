package org.taskforce.episample.supervisor.upload.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class StudyUploadViewModelFactory(private val application: Application,
                                  private val performSignIn: () -> Unit): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StudyUploadViewModel(application, 
                performSignIn) as T
    }
}