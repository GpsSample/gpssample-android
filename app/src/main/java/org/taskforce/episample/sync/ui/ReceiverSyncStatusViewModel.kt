package org.taskforce.episample.sync.ui

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.net.wifi.p2p.WifiP2pDevice
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.sync.core.WiFiDirectBroadcastCallbacks

class ReceiverSyncStatusViewModel(languageService: LanguageService,
                                  val openNetworkSettings: () -> Unit): ViewModel(), WiFiDirectBroadcastCallbacks {

    var notDiscoverableText : ObservableField<String> = ObservableField(languageService.getString(R.string.not_discoverable))
    var networkSettingsText : ObservableField<String> = ObservableField(languageService.getString(R.string.network_settings))

    init {

        languageService.update = {
            notDiscoverableText.set(languageService.getString(R.string.not_discoverable))
            discoverableText.set(languageService.getString(R.string.discoverable))
            networkSettingsText.set(languageService.getString(R.string.network_settings))
        }
    }

    var discoverable = ObservableField(false)
    var deviceName = ObservableField("")

    var discoverableVisibility = object: ObservableField<Boolean>(discoverable) {
        override fun get() = discoverable.get() == true
    }

    var notDiscoverableVisibility = object: ObservableField<Boolean>(discoverable) {
        override fun get(): Boolean = discoverable.get() == false
    }

    var discoverableText = object: ObservableField<String>(discoverable, deviceName) {
        override fun get() = languageService.getString(R.string.discoverable, deviceName.get() ?: "")
    }

    override fun stateChanged(isEnabled: Boolean) {
        discoverable.set(isEnabled)
    }

    override fun deviceChanged(device: WifiP2pDevice) {
        deviceName.set(device.deviceName)
    }
}
