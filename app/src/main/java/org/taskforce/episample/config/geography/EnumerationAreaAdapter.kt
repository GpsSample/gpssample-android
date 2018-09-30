package org.taskforce.episample.config.geography

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.EnumerationArea
import org.taskforce.episample.databinding.ItemConfigGeographyAreaBinding
import org.taskforce.episample.databinding.ItemConfigGeographyLayerBinding
import org.taskforce.episample.utils.inflater

class EnumerationAreaAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), EnumerationAreaViewModel.EnumerationActionCallbacks {
    
    var data: MutableList<Pair<EnumerationArea, Boolean>>? = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onDatasetChangedListener: OnDatasetChangedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            EnumerationAreaViewHolder(ItemConfigGeographyAreaBinding.inflate(parent.context.inflater))

    override fun getItemCount() = data?.size ?: 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val area = data!![position].first
        val isQuickstart = data!![position].second
        when (holder) {
            is EnumerationAreaViewHolder -> holder.bind(area, this, isQuickstart)
        }
    }

    override fun onDelete(enumerationArea: EnumerationArea) {
        val areaToDelete = data?.firstOrNull { it.first == enumerationArea }
        if(data?.remove(areaToDelete) == true) {
            notifyDataSetChanged()
            onDatasetChangedListener?.onDatasetChanged()
        }
    }
}

class EnumerationAreaViewHolder(private val binding: ItemConfigGeographyAreaBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(area: EnumerationArea,
             listener: EnumerationAreaViewModel.EnumerationActionCallbacks,
             isQuickstart: Boolean) {
        binding.vm = EnumerationAreaViewModel(area, listener, isQuickstart)
    }
}

class EnumerationLayerViewHolder(private val binding: ItemConfigGeographyLayerBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(isExpanded: Boolean,
             depth: Int,
             layer: EnumerationLayer) {
        binding.vm = EnumerationLayerViewModel(
                depth,
                "${layer.name} (${layer.elementCount})",
                if (isExpanded) {
                    R.drawable.icon_chevron_down_darkgray_24
                } else {
                    R.drawable.icon_chevron_right_darkgray_24
                })
    }
}

interface OnDatasetChangedListener {
    fun onDatasetChanged()
}