package org.taskforce.episample.collection.ui

import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.ViewGroup
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.viewmodels.CollectDetailField
import org.taskforce.episample.databinding.ItemCollectDetailFieldBinding
import org.taskforce.episample.utils.inflater
import org.taskforce.episample.utils.italicSubstring

class CollectDetailItemAdapter(private var iconFactory: CollectIconFactory) : RecyclerView.Adapter<CollectDetailItemViewHolder>() {
    var data: MutableList<CollectDetailField> = mutableListOf()
    var resources: Resources? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectDetailItemViewHolder {
        val binding = DataBindingUtil.inflate<ItemCollectDetailFieldBinding>(parent.context.inflater,
                R.layout.item_collect_detail_field,
                parent,
                false)
        return CollectDetailItemViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: CollectDetailItemViewHolder, position: Int) {
        val collectDetailField = data[position]

        val showIcon = collectDetailField.isCheckbox ||
                collectDetailField.isIncomplete ||
                collectDetailField.showLandmarkType

        val showText = collectDetailField.isIncomplete ||
                (collectDetailField.value.isNotBlank() && !collectDetailField.isCheckbox)

        val icon = when {
            collectDetailField.isCheckbox -> {
                iconFactory.getResourceUri(R.drawable.icon_check)
            }
            collectDetailField.isIncomplete -> {
                iconFactory.getResourceUri(R.drawable.icon_incomplete_alert)
            }
            else -> iconFactory.getIconUri(collectDetailField.data)
        }
        
        val value = collectDetailField.value
        var formattedString: SpannableStringBuilder = SpannableStringBuilder.valueOf(value)
        if (value == "Empty" || value == "Unchecked") {
            formattedString = italicSubstring(value, value)
        }

        val detailFieldViewModel = CollectDetailFieldViewModel(collectDetailField.name,
                formattedString,
                showIcon,
                showText,
                icon)
        holder.bind(detailFieldViewModel)
    }
}

class CollectDetailItemViewHolder(val binding: ItemCollectDetailFieldBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(detailField: CollectDetailFieldViewModel) {
        binding.vm = detailField
    }
}