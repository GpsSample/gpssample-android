package org.taskforce.episample.config.base

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferManager

class ConfigAllViewModelFactory(private val application: Application,
                                private val languageService: LanguageService,
                                private val transferManager: TransferManager,
                                private val createStudy: (org.taskforce.episample.db.config.Config) -> Unit,
                                private val openConfigEdit: () -> Unit,
                                private val showError: (String) -> Unit) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ConfigAllViewModel(application,
                languageService,
                transferManager,
                createStudy,
                openConfigEdit,
                showError) as T
    }
}