package org.taskforce.episample.mapbox

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.databinding.ItemMapLayerBinding
import org.taskforce.episample.utils.inflater

data class MapboxLayerSetting(val name: String, var isChecked: Boolean)

class MapboxLayersAdapter(private val settingUpdated: (MapboxLayerSetting) -> Unit) : RecyclerView.Adapter<MapboxLayerViewHolder>() {

    fun setLayers(layers: List<MapboxLayerSetting>) {
        data = layers
    }

    private var data = listOf<MapboxLayerSetting>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapboxLayerViewHolder {
        return MapboxLayerViewHolder(ItemMapLayerBinding.inflate(parent.context.inflater))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MapboxLayerViewHolder, position: Int) {
        val item = MapboxLayerItem(data[position].name,
                data[position].isChecked,
        { isChecked ->
            data[position].isChecked = isChecked
            settingUpdated(data[position])
        })

        holder.bind(item)
    }
}

class MapboxLayerViewHolder(val binding: ItemMapLayerBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(vm: MapboxLayerItem) {
        binding.vm = vm
    }
}