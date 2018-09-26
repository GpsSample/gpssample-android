package org.taskforce.episample.sync.ui

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.databinding.ItemPeerBinding
import org.taskforce.episample.sync.core.PeerState
import org.taskforce.episample.utils.inflater

class PeersAdapter(private val connect: (WifiP2pDevice) -> Unit,
                   private val disconnect: () -> Unit) : RecyclerView.Adapter<PeerViewHolder>() {

    fun setPeers(peers: List<PeerState>) {
        data = peers
    }

    private var data = listOf<PeerState>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        return PeerViewHolder(ItemPeerBinding.inflate(parent.context.inflater))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        holder.bind(PeerItemViewModel(data[position], {
            connect(data[position].device)
        }, {
            disconnect()
        }))
    }
}

class PeerViewHolder(val binding: ItemPeerBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(vm: PeerItemViewModel) {
        binding.vm = vm
    }
}