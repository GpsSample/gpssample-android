package org.taskforce.episample.sampling.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_samples.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.databinding.FragmentSamplesBinding
import org.taskforce.episample.databinding.ItemNavigationPlanBinding
import org.taskforce.episample.db.navigation.ResolvedNavigationPlan
import org.taskforce.episample.navigation.ui.NavigationPlanItemViewModel
import org.taskforce.episample.navigation.ui.NavigationPlanViewHolder
import org.taskforce.episample.utils.inflater
import javax.inject.Inject


class SamplesFragment : Fragment(), Observer<List<ResolvedNavigationPlan>> {
    @Inject
    lateinit var config: Config
    @Inject
    lateinit var collectManager: CollectManager

    lateinit var adapter: NavigationPlanAdapter
    lateinit var navigationPlans: LiveData<List<ResolvedNavigationPlan>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).collectComponent?.inject(this)
        navigationPlans = collectManager.getNavigationPlans()
        adapter = NavigationPlanAdapter(config.enumerationSubject)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentSamplesBinding>(inflater, R.layout.fragment_samples, container, false)
        binding.vm = ViewModelProviders.of(this, SamplesFragmentViewModelFactory(resources, config.enumerationSubject, collectManager, config.displaySettings))
                .get(SamplesFragmentViewModel::class.java)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationPlanRecyclerView.layoutManager = LinearLayoutManager(context)
        navigationPlanRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        navigationPlans.observe(this, this)
    }

    override fun onPause() {
        super.onPause()
        navigationPlans.removeObserver(this)
    }

    override fun onChanged(t: List<ResolvedNavigationPlan>?) {
        t?.let {
            adapter.data.clear()
            adapter.data.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }
}

class NavigationPlanAdapter(val enumerationSubject: EnumerationSubject) : RecyclerView.Adapter<NavigationPlanViewHolder>() {
    var data = mutableListOf<ResolvedNavigationPlan>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavigationPlanViewHolder(ItemNavigationPlanBinding.inflate(parent.context.inflater, parent, false))

    override fun getItemCount() = data.size


    override fun onBindViewHolder(holder: NavigationPlanViewHolder, position: Int) {
        val plan = data[position]
        val itemViewModel = NavigationPlanItemViewModel(plan.title,
                "${plan.navigationItems.size} ${enumerationSubject.plural}", null, null)
        holder.bind(itemViewModel)
    }
}

