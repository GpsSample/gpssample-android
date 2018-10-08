package org.taskforce.episample.navigation.ui

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.core.interfaces.NavigationPlan
import org.taskforce.episample.databinding.ItemNavigationPlanBinding
import org.taskforce.episample.utils.inflater

class NavigationPlanAdapter(val enumerationSubject: EnumerationSubject,
                            val showPlan: (NavigationPlan) -> Unit) : RecyclerView.Adapter<NavigationPlanViewHolder>() {
    var data = mutableListOf<NavigationPlan>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavigationPlanViewHolder(ItemNavigationPlanBinding.inflate(parent.context.inflater, parent, false))

    override fun getItemCount() = data.size


    override fun onBindViewHolder(holder: NavigationPlanViewHolder, position: Int) {
        val plan = data[position]
        val itemViewModel = NavigationPlanItemViewModel(plan.title,
                "${plan.navigationItems.size} ${enumerationSubject.plural}",
                plan) {
            showPlan(it)
        }
        holder.bind(itemViewModel)
    }
}

class NavigationPlanViewHolder(val binding: ItemNavigationPlanBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(itemViewModel: NavigationPlanItemViewModel) {
        binding.vm = itemViewModel
    }
}