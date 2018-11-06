package org.taskforce.episample.navigation.ui

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mapbox.mapboxsdk.geometry.LatLng
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.core.interfaces.NavigationItem
import org.taskforce.episample.core.util.DistanceUtil
import org.taskforce.episample.databinding.ItemNavigationBinding
import org.taskforce.episample.utils.inflater

class NavigationItemAdapter(
        private val collectIconFactory: CollectIconFactory,
        val displaySettings: DisplaySettings,
        private val lifecycleOwner: LifecycleOwner,
        val viewDetails: (navigationItem: NavigationItem) -> Unit) : RecyclerView.Adapter<NavigationItemViewHolder>() {

    var data = listOf<NavigationItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    
    var location = MutableLiveData<LatLng>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavigationItemViewHolder(ItemNavigationBinding.inflate(parent.context.inflater, parent, false), lifecycleOwner)

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: NavigationItemViewHolder, position: Int) {
        val navigationItem = data[position]

        val distance = Transformations.map(location) {
            val results = FloatArray(3)
            val itemLocation = navigationItem.location
            Location.distanceBetween(it.latitude,
                    it.longitude,
                    itemLocation.latitude,
                    itemLocation.longitude,
                    results)

            "Distance ${DistanceUtil.convertMetersToString(results[0])}"
        }
        
        holder.bind(NavigationItemViewModel(
                position,
                navigationItem.surveyStatus,
                navigationItem.title,
                distance,
                navigationItem,
                viewDetails))
    }
}

// TODO show navigate specific layout
class NavigationItemViewHolder(val binding: ItemNavigationBinding, val lifecycleOwner: LifecycleOwner): RecyclerView.ViewHolder(binding.root) {
    fun bind(vm: NavigationItemViewModel) {
        binding.setLifecycleOwner(lifecycleOwner)
        binding.vm = vm
    }
    
}