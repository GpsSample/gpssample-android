package org.taskforce.episample.navigation.ui

import android.arch.lifecycle.MutableLiveData
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.viewmodels.CollectItemViewModel
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.core.interfaces.NavigationItem
import org.taskforce.episample.databinding.ItemCollectBinding
import org.taskforce.episample.databinding.ItemNavigationBinding
import org.taskforce.episample.utils.inflater

class NavigationItemAdapter(
        private val collectIconFactory: CollectIconFactory,
        val displaySettings: DisplaySettings) : RecyclerView.Adapter<NavigationItemViewHolder>() {

    var data = listOf<NavigationItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavigationItemViewHolder(ItemNavigationBinding.inflate(parent.context.inflater, parent, false))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: NavigationItemViewHolder, position: Int) {
        holder.bind(NavigationItemViewModel(
                position,
                data[position].surveyStatus,
                data[position].title,
                MutableLiveData<String>().apply { value = "Distance TODO" }))
    }
}

// TODO show navigate specific layout
class NavigationItemViewHolder(val binding: ItemNavigationBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(vm: NavigationItemViewModel) {
        binding.vm = vm
    }
}