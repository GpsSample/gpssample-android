package org.taskforce.episample.sync.ui

import android.net.wifi.p2p.WifiP2pDevice
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.databinding.ItemEnumeratorBinding
import org.taskforce.episample.databinding.ItemPeerBinding
import org.taskforce.episample.sync.core.PeerState
import org.taskforce.episample.utils.inflater

class EnumeratorsAdapter(private val connect: (WifiP2pDevice) -> Unit,
                         private val disconnect: () -> Unit) : RecyclerView.Adapter<EnumeratorViewHolder>() {

    fun setPeers(peers: List<PeerState>) {
        data = peers
    }

    private var data = listOf<PeerState>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnumeratorViewHolder {
        return EnumeratorViewHolder(ItemEnumeratorBinding.inflate(parent.context.inflater))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: EnumeratorViewHolder, position: Int) {
        holder.bind(EnumeratorItemViewModel(data[position], {
            connect(data[position].device)
        }, {
            disconnect()
        }))
    }
}

class EnumeratorViewHolder(val binding: ItemEnumeratorBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(vm: EnumeratorItemViewModel) {
        binding.vm = vm
    }
}