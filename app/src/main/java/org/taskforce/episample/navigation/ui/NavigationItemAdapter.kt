package org.taskforce.episample.navigation.ui

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.viewmodels.CollectItemViewModel
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.databinding.ItemCollectBinding
import org.taskforce.episample.utils.inflater

class NavigationItemAdapter(
        private val collectIconFactory: CollectIconFactory,
        val displaySettings: DisplaySettings) : RecyclerView.Adapter<CollectItemViewHolder>() {

    var data = listOf<Enumeration>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CollectItemViewHolder(ItemCollectBinding.inflate(parent.context.inflater, parent, false))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: CollectItemViewHolder, position: Int) {
        holder.bind(CollectItemViewModel(
                data[position],
                collectIconFactory.getIconUriFromCollectItem(data[position]),
                "", // TODO refactor to not have concept of incomplete in Navigation
                displaySettings))
    }
}

// TODO show navigate specific layout
class CollectItemViewHolder(val binding: ItemCollectBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(vm: CollectItemViewModel) {
        binding.vm = vm
    }
}