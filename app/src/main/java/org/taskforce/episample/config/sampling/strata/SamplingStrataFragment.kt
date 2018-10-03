package org.taskforce.episample.config.sampling.strata

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_config_sampling_subset.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.AllStrataUpdated
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.sampling.filter.RuleSetCardViewModel
import org.taskforce.episample.config.sampling.filter.RuleSetCreationActivity
import org.taskforce.episample.config.sampling.subsets.SamplingSubsetViewModel
import org.taskforce.episample.config.sampling.subsets.SamplingSubsetViewModelFactory
import org.taskforce.episample.config.sampling.subsets.SubsetAdapter
import org.taskforce.episample.databinding.FragmentConfigSamplingStrataBinding
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.db.sampling.strata.Strata
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.makeDBConfig
import javax.inject.Inject


class SamplingStrataFragment : Fragment(), Observer<SamplingSubsetViewModel.Event>, SubsetAdapter.SubsetSelectedListener {
    @Inject
    lateinit var languageManager: LanguageManager

    private lateinit var configBuildViewModel: ConfigBuildViewModel
    private lateinit var adapter: SubsetAdapter
    private var viewModel: SamplingSubsetViewModel? = null
    private val eventBus = EventBus.getDefault()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)
        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)
        adapter = SubsetAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentConfigSamplingStrataBinding>(inflater, R.layout.fragment_config_sampling_strata, container, false)
        binding.headerVm = ConfigHeaderViewModel(LanguageService(languageManager), R.string.config_sampling_subset_title, R.string.config_sampling_subset_explanation)
        viewModel = ViewModelProviders.of(requireActivity(), SamplingSubsetViewModelFactory())
                .get(SamplingSubsetViewModel::class.java).apply {
                    events.observe(this@SamplingStrataFragment, this@SamplingStrataFragment)
                }
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subsetRecyclerView.layoutManager = LinearLayoutManager(context)
        subsetRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        eventBus.unregister(this)
        viewModel?.events?.removeObservers(this)
        viewModel?.events?.value = null
    }

    @Subscribe(sticky = true)
    fun onAllStrataReceived(strataUpdated: AllStrataUpdated) {
        val viewModels = strataUpdated.ruleSets.map { ruleSet ->
            val ruleCount = strataUpdated.ruleRecords.count {
                it.ruleSetId == ruleSet.id
            }
            RuleSetCardViewModel(ruleSet.id, ruleSet.name, ruleCount, ruleSet.isAny)
        }
        adapter.setData(viewModels)
        viewModel?.nextEnabled?.set(viewModels.isNotEmpty())
    }

    override fun onChanged(t: SamplingSubsetViewModel.Event?) {
        when (t) {
            //TODO : Examine why the fieldIds are changing
            is SamplingSubsetViewModel.Event.AddRuleSet -> {
                RuleSetCreationActivity.startActivity(this, configBuildViewModel.configBuildManager.config.customFields.map {
                    it.makeDBConfig(configBuildViewModel.configBuildManager.config.id)
                }, configBuildViewModel.configBuildManager.config.id)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RuleSetCreationActivity.REQUEST_CODE_FOR_RULESET && resultCode == Activity.RESULT_OK) {
            data?.let {
                val ruleSet = it.getParcelableExtra(RuleSetCreationActivity.EXTRA_RESULT_RULESET) as RuleSet
                @Suppress("UNCHECKED_CAST")
                val rules = it.getParcelableArrayExtra(RuleSetCreationActivity.EXTRA_RESULT_RULES).toList() as List<RuleRecord>

                val newStrata = Strata(configBuildViewModel.configBuildManager.config.id, ruleSet.id)

                eventBus.post(StrataUpdated(newStrata, ruleSet, rules))
            }
        }
    }

    @Suppress("UNREACHABLE_CODE")
    override fun onSubsetSelected(ruleSetCardViewModel: RuleSetCardViewModel) {
        //TODO remove this return when you want to get back to working on editing the rulesets
        return
        //open ruleset creation activity with the subset
        val ruleSetId = ruleSetCardViewModel.ruleSetId
        val ruleSet = configBuildViewModel.configBuildManager.config.ruleSets.find {
            it.id == ruleSetId
        }
        val rules = configBuildViewModel.configBuildManager.config.rules.filter {
            it.ruleSetId == ruleSetId
        }

        if (ruleSet != null) {
            RuleSetCreationActivity.startActivity(this, configBuildViewModel.configBuildManager.config.customFields.map {
                it.makeDBConfig(configBuildViewModel.configBuildManager.config.id)
            }, configBuildViewModel.configBuildManager.config.id, ruleSet, rules)
        }
    }

    companion object {
        const val HELP_TARGET = "#samplingSubset"
    }
}

class StrataUpdated(val strata: Strata, val ruleSet: RuleSet, val rules: List<RuleRecord>)