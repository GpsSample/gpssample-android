package org.taskforce.episample.sync.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
class WiFiDirectBroadcastReceiver(): BroadcastReceiver() {

    init {

    }

    constructor(manager: WifiP2pManager, channel: WifiP2pManager.Channel,
                p2pListener: WiFiDirectBroadcastCallbacks) : this() {
        this.mManager = manager
        this.mChannel = channel
        this.p2pListener = p2pListener
    }

    lateinit var p2pListener: WiFiDirectBroadcastCallbacks

    lateinit var mManager: WifiP2pManager
    lateinit var mChannel: WifiP2pManager.Channel

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    p2pListener.stateChanged(true)
                } else {
                    p2pListener.stateChanged(false)
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                p2pListener.peersChanged()

            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo

                p2pListener.connectionChanged(networkInfo)
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val device = intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)

                p2pListener.deviceChanged(device)
            }
        }
    }
}

interface WiFiDirectBroadcastCallbacks {
    fun stateChanged(isEnabled: Boolean)
    fun peersChanged()
    fun connectionChanged(networkInfo: NetworkInfo)
    fun deviceChanged(device: WifiP2pDevice)
}