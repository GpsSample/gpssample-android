package org.taskforce.episample.sync.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.sync.core.DirectTransferService
import org.taskforce.episample.sync.core.DirectTransferServiceMode
import org.taskforce.episample.sync.core.LiveDirectTransferService

class ReceiverSyncStatusViewModel(application: Application,
                                  languageService: LanguageService,
                                  val openNetworkSettings: () -> Unit): AndroidViewModel(application) {

    val directTransferService: DirectTransferService = LiveDirectTransferService(application, DirectTransferServiceMode.RECEIVE_STUDY)

    var notDiscoverableText : ObservableField<String> = ObservableField(languageService.getString(R.string.not_discoverable))
    var networkSettingsText : ObservableField<String> = ObservableField(languageService.getString(R.string.network_settings))

    var discoverable = directTransferService.isEnabledLiveData

    var deviceName = directTransferService.deviceNameLiveData

    var discoverableVisibility = Transformations.map(discoverable, { it })

    var notDiscoverableVisibility = Transformations.map(discoverable, { !it })

    var discoverableText = Transformations.map(deviceName) {
        languageService.getString(R.string.discoverable, it)
    }
}
