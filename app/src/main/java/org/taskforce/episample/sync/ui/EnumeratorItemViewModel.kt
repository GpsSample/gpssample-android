package org.taskforce.episample.sync.ui

import android.view.View
import org.taskforce.episample.sync.core.PeerState

class EnumeratorItemViewModel(peerState: PeerState,
                              val connect: () -> Unit,
                              val disconnect: () -> Unit) {

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
