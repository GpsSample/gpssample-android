package org.taskforce.episample.navigation.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.taskforce.episample.R
import org.taskforce.episample.databinding.FragmentNavigationListBinding

class NavigationListFragment: Fragment() {
    
    lateinit var viewModel: NavigationListViewModel
    lateinit var toolbarViewModel: NavigationToolbarViewModel
    lateinit var planAdapter: NavigationPlanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = ViewModelProviders.of(this).get(NavigationListViewModel::class.java)

        toolbarViewModel = ViewModelProviders.of(this,
                NavigationToolbarViewModelFactory(requireActivity().application,
                        R.string.navigation_plan)).get(NavigationToolbarViewModel::class.java)
        
        planAdapter = NavigationPlanAdapter(viewModel.config.enumerationSubject) {
            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, NavigationPlanFragment.newInstance(it.id))
                    .addToBackStack(NavigationListFragment::class.qualifiedName)
                    .commit()
            
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentNavigationListBinding>(inflater, R.layout.fragment_navigation_list, container, false)
        binding.vm = viewModel
        binding.toolbarVm = toolbarViewModel
        
        binding.chooseNavigationPlanList.layoutManager = LinearLayoutManager(requireContext())
        binding.chooseNavigationPlanList.adapter = planAdapter
        
        viewModel.navigationPlans.observe(this, Observer { 
            planAdapter.data = it?.toMutableList() ?: mutableListOf()
            planAdapter.notifyDataSetChanged()
        })
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        toolbar?.setNavigationOnClickListener {
            if (fragmentManager?.backStackEntryCount ?: 0 > 0) {
                fragmentManager?.popBackStack()
            } else {
                requireActivity().finish()
            }
        }
    }
    
    companion object {
        fun newInstance(): Fragment {
            return NavigationListFragment()
        }
    }
}