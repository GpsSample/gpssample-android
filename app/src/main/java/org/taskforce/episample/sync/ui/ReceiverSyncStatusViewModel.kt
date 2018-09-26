package org.taskforce.episample.sync.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pInfo
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.sync.core.DirectTransferService
import org.taskforce.episample.sync.core.DirectTransferServiceCallbacks
import org.taskforce.episample.sync.core.LiveDirectTransferService

class ReceiverSyncStatusViewModel(application: Application,
                                  languageService: LanguageService,
                                  val openNetworkSettings: () -> Unit): AndroidViewModel(application), DirectTransferServiceCallbacks {

    val directTransferService: DirectTransferService = LiveDirectTransferService(application, intendToOwnGroup = true)

    var notDiscoverableText : ObservableField<String> = ObservableField(languageService.getString(R.string.not_discoverable))
    var networkSettingsText : ObservableField<String> = ObservableField(languageService.getString(R.string.network_settings))

    var discoverable = directTransferService.isEnabledLiveData

    var deviceName = directTransferService.deviceNameLiveData

    var discoverableVisibility = Transformations.map(discoverable, { it })

    var notDiscoverableVisibility = Transformations.map(discoverable, { !it })

    var discoverableText = Transformations.map(deviceName) {
        languageService.getString(R.string.discoverable, it)
    }

    override fun onConnectionInfoUpdate(info: WifiP2pInfo?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onGroupInfoUpdate(group: WifiP2pGroup?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectedPeerListUpdate(deviceList: WifiP2pDeviceList) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
