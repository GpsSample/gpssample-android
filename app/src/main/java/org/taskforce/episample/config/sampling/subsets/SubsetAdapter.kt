package org.taskforce.episample.config.sampling.subsets

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.R
import org.taskforce.episample.config.sampling.SamplingUnits
import org.taskforce.episample.config.sampling.filter.RuleSetCardViewModel
import org.taskforce.episample.databinding.ItemSubsetCardBinding


class SubsetAdapter : RecyclerView.Adapter<SubsetAdapter.SubsetCardViewHolder>() {
    private val viewModels = mutableListOf<RuleSetCardViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubsetCardViewHolder {
        return SubsetCardViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_subset_card, parent, false))
    }

    override fun getItemCount(): Int = viewModels.size

    override fun onBindViewHolder(holder: SubsetCardViewHolder, position: Int) {
        holder.bind(viewModels[position])
    }

    fun setData(data: List<RuleSetCardViewModel>) {
        viewModels.clear()
        viewModels.addAll(data)
        notifyDataSetChanged()
    }

    fun samplingUnitsChanged(samplingUnit: SamplingUnits) {
        viewModels.forEach {
            it.isUnitPercent = samplingUnit == SamplingUnits.PERCENT
        }
    }

    fun isValid(): Boolean {
        if (viewModels.size == 0) {
            return false
        }
        viewModels.forEach {
            if (!it.isValid())
                return false
        }
        return true
    }

    class SubsetCardViewHolder(val binding: ItemSubsetCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: RuleSetCardViewModel) {
            binding.vm = viewModel
        }
    }
}
