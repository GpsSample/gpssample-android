package org.taskforce.episample.sync.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager.*

class WiFiDirectBroadcastReceiver(private val callbacks: WiFiDirectBroadcastCallbacks) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WIFI_P2P_STATE_CHANGED_ACTION -> {
                callbacks.stateChanged(intent.getIntExtra(EXTRA_WIFI_STATE, -1) == WIFI_P2P_STATE_ENABLED)
            }
            WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                callbacks.deviceChanged(intent.getParcelableExtra(EXTRA_WIFI_P2P_DEVICE))
            }
        }
    }
}


interface WiFiDirectBroadcastCallbacks {
    fun stateChanged(isEnabled: Boolean)
    fun deviceChanged(device: WifiP2pDevice)
}