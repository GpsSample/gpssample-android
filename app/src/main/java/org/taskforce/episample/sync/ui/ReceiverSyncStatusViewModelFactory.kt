package org.taskforce.episample.sync.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.language.LanguageService

class ReceiverSyncStatusViewModelFactory(private val application: Application,
                                         private val languageService: LanguageService,
                                         private val openNetworkSettings: () -> Unit) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReceiverSyncStatusViewModel(application,
                languageService,
                openNetworkSettings) as T
    }

}