package org.taskforce.episample.sync.core

import android.content.ContextWrapper
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager


class DirectTransferService(private val callbacks: WiFiDirectBroadcastCallbacks):
        WiFiDirectBroadcastCallbacks {

    private val receiver = WiFiDirectBroadcastReceiver(this)

    fun registerReceiver(context: ContextWrapper) {
        context.registerReceiver(receiver, intentFilter)
    }

    fun unregisterReceiver(context: ContextWrapper) {
        context.unregisterReceiver(receiver)
    }

    override fun stateChanged(isEnabled: Boolean) {
        callbacks.stateChanged(isEnabled)
    }

    override fun deviceChanged(device: WifiP2pDevice) {
        callbacks.deviceChanged(device)

        // TODO discover peers here
    }

    companion object {

        val intentFilter = IntentFilter().apply {
            this.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            this.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            this.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            this.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }
}