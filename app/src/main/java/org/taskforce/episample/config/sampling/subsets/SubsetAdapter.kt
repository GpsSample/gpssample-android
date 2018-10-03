package org.taskforce.episample.config.sampling.subsets

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.R
import org.taskforce.episample.config.sampling.filter.RuleSetCardViewModel
import org.taskforce.episample.databinding.ItemSubsetCardBinding


class SubsetAdapter(private val subsetSelectedListener: SubsetSelectedListener) : RecyclerView.Adapter<SubsetAdapter.SubsetCardViewHolder>() {
    private val viewModels = mutableListOf<RuleSetCardViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubsetCardViewHolder {
        return SubsetCardViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_subset_card, parent, false), subsetSelectedListener)
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

    class SubsetCardViewHolder(val binding: ItemSubsetCardBinding, val onClickListener: SubsetSelectedListener) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                binding.vm?.let { viewModel -> onClickListener.onSubsetSelected(viewModel) }
            }
        }

        fun bind(viewModel: RuleSetCardViewModel) {
            binding.vm = viewModel
        }
    }

    interface SubsetSelectedListener {
        fun onSubsetSelected(ruleSetCardViewModel: RuleSetCardViewModel)
    }
}
