package org.taskforce.episample.config.geography

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.R
import org.taskforce.episample.databinding.ItemConfigGeographyAreaBinding
import org.taskforce.episample.databinding.ItemConfigGeographyLayerBinding
import org.taskforce.episample.utils.inflater

class EnumerationAreaAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), EnumerationAreaViewModel.EnumerationActionCallbacks {
    var data: EnumerationLayer? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    val dataSize: Int
        get() = data?.elementCount ?: 0

    var onDatasetChangedListener: OnDatasetChangedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                0 -> EnumerationLayerViewHolder(ItemConfigGeographyLayerBinding.inflate(parent.context.inflater))
                else -> {
                    EnumerationAreaViewHolder(ItemConfigGeographyAreaBinding.inflate(parent.context.inflater))
                }
            }

    override fun getItemCount() = dataSize

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EnumerationAreaViewHolder -> holder.bind(0, data?.enumerationAreas!![0], this)
        //is EnumerationLayerViewHolder -> holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    override fun onDelete(enumerationArea: EnumerationArea) {
        if(data?.remove(enumerationArea) == true) {
            notifyDataSetChanged()
            onDatasetChangedListener?.onDatasetChanged()
        }
    }
}

class EnumerationAreaViewHolder(private val binding: ItemConfigGeographyAreaBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(depth: Int,
             area: EnumerationArea,
             listener: EnumerationAreaViewModel.EnumerationActionCallbacks) {
        binding.vm = EnumerationAreaViewModel(area, depth, listener)
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