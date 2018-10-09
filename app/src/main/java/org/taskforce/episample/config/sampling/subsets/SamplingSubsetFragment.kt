package org.taskforce.episample.config.sampling.subsets

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
import org.taskforce.episample.config.base.AllSubsetsUpdated
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.sampling.SamplingUnits
import org.taskforce.episample.config.sampling.filter.RuleSetCardViewModel
import org.taskforce.episample.config.sampling.filter.RuleSetCreationActivity
import org.taskforce.episample.databinding.FragmentConfigSamplingSubsetBinding
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.makeDBConfig
import javax.inject.Inject

class SamplingSubsetFragment : Fragment(), Observer<SamplingSubsetViewModel.Event> {

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
        adapter = SubsetAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentConfigSamplingSubsetBinding>(inflater, R.layout.fragment_config_sampling_subset, container, false)
        binding.headerVm = ConfigHeaderViewModel(LanguageService(languageManager), R.string.config_sampling_subset_title, R.string.config_sampling_subset_explanation)
        viewModel = ViewModelProviders.of(requireActivity(), SamplingSubsetViewModelFactory(configBuildViewModel.configBuildManager.config.samplingMethod.units == SamplingUnits.FIXED))
                .get(SamplingSubsetViewModel::class.java)
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
        viewModel?.events?.observe(this@SamplingSubsetFragment, this@SamplingSubsetFragment)
    }

    override fun onPause() {
        super.onPause()
        eventBus.unregister(this)
        viewModel?.events?.removeObservers(this)
        viewModel?.events?.value = null
    }

    @Subscribe
    fun onSamplingUnitsChanged(event: SamplingSubsetViewModel.Event.SamplingUnitsChanged) = adapter.samplingUnitsChanged(event.samplingUnit)

    @Subscribe
    fun onSamplingAmountChanged(event: RuleSetCardViewModel.SamplingAmountChanged) {
        viewModel?.nextEnabled?.set(adapter.isValid())
    }

    @Subscribe(sticky = true)
    fun onAllSubsetsReceived(subsetsUpdated: AllSubsetsUpdated) {
        val viewModels = subsetsUpdated.ruleSets.map { ruleSet ->
            val ruleCount = subsetsUpdated.ruleRecords.count {
                it.ruleSetId == ruleSet.id
            }
            RuleSetCardViewModel(ruleSet.id, ruleSet.name, ruleCount, ruleSet.isAny, configBuildViewModel.configBuildManager.config.samplingMethod.units == SamplingUnits.PERCENT, ruleSet.sampleSize)
        }
        adapter.setData(viewModels)
        viewModel?.nextEnabled?.set(adapter.isValid())
    }

    override fun onChanged(t: SamplingSubsetViewModel.Event?) {
        when (t) {
            is SamplingSubsetViewModel.Event.AddRuleSet -> {
                RuleSetCreationActivity.startActivity(this, configBuildViewModel.configBuildManager.config.customFields.map {
                    it.makeDBConfig(configBuildViewModel.configBuildManager.config.id)
                }, configBuildViewModel.configBuildManager.config.id, configBuildViewModel.configBuildManager.config.samplingMethod.id)
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

                eventBus.post(SubsetsUpdated(ruleSet, rules))
            }
        }
    }

    companion object {
        const val HELP_TARGET = "#samplingSubset"
    }
}

class SubsetsUpdated(val ruleSet: RuleSet, val rules: List<RuleRecord>)