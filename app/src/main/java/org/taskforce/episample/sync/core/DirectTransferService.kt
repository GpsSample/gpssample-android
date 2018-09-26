package org.taskforce.episample.sync.core

import android.arch.lifecycle.*
import android.content.Context
import android.content.ContextWrapper
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.*
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import org.taskforce.episample.sync.FileServerAsyncTask

data class PeerState(val device: WifiP2pDevice, val info: WifiP2pInfo?)

interface DirectTransferService : LifecycleObserver, WifiP2pManager.ConnectionInfoListener, WiFiDirectBroadcastCallbacks {

    val context: Context
    val receiver: WiFiDirectBroadcastReceiver

    val deviceNameLiveData: LiveData<String>
    val isEnabledLiveData: LiveData<Boolean>

    val peerListLiveData: LiveData<List<PeerState>>
    val connectionInfoLiveData: LiveData<WifiP2pInfo?>
    val groupInfoLiveData: LiveData<WifiP2pGroup?>

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun connectListener() {
        context.registerReceiver(receiver, DirectTransferService.intentFilter)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun disconnectListener() {
        removeGroup()
        context.unregisterReceiver(receiver)
    }

    fun connect(config: WifiP2pConfig, context: ContextWrapper)
    fun refreshPeers()
    fun removeGroup()

    companion object {
        val intentFilter = IntentFilter().apply {
            this.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            this.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            this.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            this.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }
}

class LiveDirectTransferService(override val context: ContextWrapper,
                                private val intendToOwnGroup: Boolean) : DirectTransferService {

    override val deviceNameLiveData = MutableLiveData<String>().apply { value = "" }
    override val isEnabledLiveData = MutableLiveData<Boolean>().apply { value = false }

    override val peerListLiveData = MutableLiveData<List<PeerState>>()
    override val connectionInfoLiveData = MutableLiveData<WifiP2pInfo?>()
    override val groupInfoLiveData = MutableLiveData<WifiP2pGroup?>()

    private val peers = ArrayList<WifiP2pDevice>()
    override val receiver: WiFiDirectBroadcastReceiver

    private val mChannel: WifiP2pManager.Channel
    private val mManager: WifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager

    init {
        mChannel = mManager.initialize(context, context.mainLooper, null)
        receiver = WiFiDirectBroadcastReceiver(mManager, mChannel, this)
    }

    override fun stateChanged(isEnabled: Boolean) {
        isEnabledLiveData.postValue(isEnabled)
    }

    override fun peersChanged() {
        Log.d(TAG, "WIFI P2P PEERS CHANGED ACTION")

        val peerListListener = WifiP2pManager.PeerListListener {
            val refreshedPeers = it.deviceList
            if (refreshedPeers != peers) {
                peers.clear()
                peers.addAll(refreshedPeers)
                peerListLiveData.postValue(refreshedPeers.map(this::deviceToPeer))
            }

            if (peers.size == 0) {
                Log.d("SYNC", "No devices found")
            }
        }

        mManager.requestPeers(mChannel, peerListListener)
    }

    override fun connectionChanged(networkInfo: NetworkInfo) {
        Log.d(TAG, "WIFI P2P CONNECTION CHANGED ACTION")


        if (networkInfo.isConnected) {

            // We are connected with the other device, request connection
            // info to find group owner IP

            mManager.requestConnectionInfo(mChannel, this)
        }
    }

    override fun deviceChanged(device: WifiP2pDevice) {
        Log.d(TAG, "WIFI P2P DEVICE CHANGED ACTION")
        deviceNameLiveData.postValue(device.deviceName)
        mManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Log.d(TAG, "DISCOVER PEERS INITIATION SUCCESS")
            }

            override fun onFailure(reasonCode: Int) {
                Log.d(TAG, "DISCOVER PEERS INITIATION FAILURE")
            }
        })
    }

    override fun connect(config: WifiP2pConfig, context: ContextWrapper) {
        if (intendToOwnGroup) {
            config.groupOwnerIntent = 15
        } else {
            config.groupOwnerIntent = 1
        }

        mManager.connect(mChannel, config, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Toast.makeText(context, "Connect Succeeded.",
                        Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(context, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        connectionInfoLiveData.postValue(info)
        if (info == null) {
            return
        }

        if (info.groupFormed && intendToOwnGroup) {
            FileServerAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.filesDir, context.getDatabasePath("study_database"))
        } else if (info.groupFormed) {

        }
    }

    private fun deletePersistentGroups() {
        try {
            val methods = WifiP2pManager::class.java.methods
            for (i in methods.indices) {
                if (methods[i].name.equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (netid in 0..31) {
                        methods[i].invoke(mManager, mChannel, netid, null)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun removeGroup() {
        mManager.stopPeerDiscovery(mChannel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Log.d(TAG, "Connect Succeeded.")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "Connect failed. Retry.")
            }

        })
        deletePersistentGroups()
        mManager.removeGroup(mChannel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Log.d(TAG, "Connect Succeeded.")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "Connect failed. Retry.")
            }

        })
    }

    override fun refreshPeers() {
        mManager.requestPeers(mChannel) {
            peerListLiveData.postValue(it.deviceList.map(this::deviceToPeer))
        }
    }

    fun getGroupInfo() {
        mManager.requestGroupInfo(mChannel) {
            groupInfoLiveData.postValue(it)
        }
    }

    private fun deviceToPeer(device: WifiP2pDevice): PeerState {
        val info = peerListLiveData.value?.filter({ it.device.deviceAddress == device.deviceAddress })?.map { it.info }?.firstOrNull()
        return PeerState(device, info)
    }

    companion object {
        const val TAG = "DirectTransferService"
    }
}

interface DirectTransferServiceCallbacks {
    fun onConnectionInfoUpdate(info: WifiP2pInfo?)
    fun onGroupInfoUpdate(group: WifiP2pGroup?)
    fun onConnectedPeerListUpdate(deviceList: WifiP2pDeviceList)
}