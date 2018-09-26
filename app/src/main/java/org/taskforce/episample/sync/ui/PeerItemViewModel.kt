package org.taskforce.episample.sync.ui

import android.arch.lifecycle.ViewModel
import android.net.wifi.p2p.WifiP2pDevice
import android.view.View
import org.taskforce.episample.sync.core.PeerState

class PeerItemViewModel(val peerState: PeerState,
                        val connect: () -> Unit,
                        val disconnect: () -> Unit): ViewModel() {

    val deviceName = peerState.device.deviceName
    val deviceStatus = when (peerState.device.status) {
        0 -> "Connected"
        1 -> "Invited"
        2 -> "Failed"
        3 -> "Available"
        4 -> "Unavailable"
        else -> "Unknown"
    }

    fun connect(view: View) {
        connect()
    }

    fun disconnect(view: View) {
        disconnect()
    }
}