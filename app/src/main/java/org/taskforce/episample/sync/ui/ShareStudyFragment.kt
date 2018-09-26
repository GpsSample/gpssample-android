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
import kotlinx.android.synthetic.main.fragment_share_study.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.core.util.FileUtil
import org.taskforce.episample.databinding.FragmentShareStudyBinding
import org.taskforce.episample.sync.core.FileTransferService

class ShareStudyFragment : Fragment() {

    private lateinit var viewModel: ShareStudyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ShareStudyViewModel::class.java)
        lifecycle.addObserver(viewModel.directTransferService)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentShareStudyBinding>(inflater, R.layout.fragment_share_study, container, false)

        binding.setLifecycleOwner(this)
        binding.vm = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PeersAdapter(
                { info ->
                    connectToDevice(info)
                },
                {
                    disconnect()
                })
        fragment_peers_recyclerView.adapter = adapter
        fragment_peers_recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        fragment_peers_shareButton.setOnClickListener({
            shareStudyToDevice()
        })
        fragment_peers_refreshButton.setOnClickListener({
            refreshPeers()
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

    private fun shareStudyToDevice() {
        viewModel.directTransferService.connectionInfoLiveData.value?.let { info ->
            val serviceIntent = Intent(activity, FileTransferService::class.java)
            serviceIntent.action = FileTransferService.ACTION_SEND_FILE

            val databaseName = "study_database"
            val databaseFile = (requireActivity().application as EpiApplication).getDatabasePath("study_database")
            val directory = databaseFile.parent

            val shmFile = "$directory/$databaseName-shm"
            val walFile = "$directory/$databaseName-wal"
            val zipFile = "${requireActivity().application.filesDir}/$databaseName.zip"

            FileUtil.zip(listOf<String>(databaseFile.absolutePath, shmFile, walFile).toTypedArray(), zipFile)

            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, "file://$zipFile")
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.hostAddress)
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988)
            requireActivity().startService(serviceIntent)
        }
    }


}