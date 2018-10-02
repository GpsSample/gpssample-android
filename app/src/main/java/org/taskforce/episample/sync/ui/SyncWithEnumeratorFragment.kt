package org.taskforce.episample.sync.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_sync_with_enumerator.*
import org.taskforce.episample.R
import org.taskforce.episample.databinding.FragmentSyncWithEnumeratorBinding
import org.taskforce.episample.sync.core.ReceiveFileTransferService

class SyncWithEnumeratorFragment: Fragment() {

    lateinit var viewModel: SyncWithEnumeratorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(SyncWithEnumeratorViewModel::class.java)
        lifecycle.addObserver(viewModel.directTransferService)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentSyncWithEnumeratorBinding>(inflater, R.layout.fragment_sync_with_enumerator, container, false)

        binding.vm = viewModel
        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = EnumeratorsAdapter(
                { info ->
                    connectToDevice(info)
                },
                {
                    disconnect()
                })

        fragment_sync_with_enumerator_recyclerView.adapter = adapter
        fragment_sync_with_enumerator_recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        fragment_sync_with_enumerator_refreshButton.setOnClickListener({
            refreshPeers()
        })

        fragment_sync_with_enumerator_shareButton.setOnClickListener({
            syncWithEnumerator()
        })

        viewModel.directTransferService.peerListLiveData.observe(this, Observer { peers ->
            adapter.setPeers(peers ?: listOf())
        })
    }

    private fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        config.wps.setup = WpsInfo.PBC

        viewModel.directTransferService.connect(config, requireActivity())
    }

    private fun disconnect() {
        viewModel.directTransferService.removeGroup()
    }

    private fun refreshPeers() {
        viewModel.directTransferService.refreshPeers()
    }

    private fun syncWithEnumerator() {
        viewModel.directTransferService.connectionInfoLiveData.value?.let { info ->
            val serviceIntent = Intent(activity, ReceiveFileTransferService::class.java)
            serviceIntent.action = ReceiveFileTransferService.ACTION_RECEIVE_FILE
            serviceIntent.putExtra(ReceiveFileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.hostAddress)
            serviceIntent.putExtra(ReceiveFileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988)
            requireActivity().startService(serviceIntent)
        }
    }


    companion object {
        fun newInstance(): Fragment {
            return SyncWithEnumeratorFragment()
        }
    }

}
