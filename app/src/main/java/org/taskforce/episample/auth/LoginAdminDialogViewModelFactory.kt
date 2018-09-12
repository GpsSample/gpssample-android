package org.taskforce.episample.auth

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class LoginAdminDialogViewModelFactory(private val application: Application,
                                       private val configId: String,
                                       private val title: String,
                                       private val hint: String,
                                       private val cancel: String,
                                       private val done: String,
                                       private val errorSource: String,
                                       private val onCancel: () -> Unit,
                                       private val onDone: () -> Unit) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LoginAdminDialogViewModel(application,
                configId,
                title,
                hint,
                cancel,
                done,
                errorSource,
                onCancel,
                onDone) as T
    }
}