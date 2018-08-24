package org.taskforce.episample.config.base

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.db.config.Config
class ConfigStartViewModelFactory(private val application: Application,
                                  val createNewConfiguration: () -> Unit,
                                  val showAllConfigurations: () -> Unit,
                                  val editStudy: () -> Unit,
                                  val signIn: (Config, String) -> Unit,
                                  val transferManager: TransferManager) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ConfigStartViewModel(application,
                createNewConfiguration,
                showAllConfigurations,
                editStudy,
                signIn,
                transferManager) as T
    }
}