package org.taskforce.episample.config.sampling.no_grouping

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_config_sampling_no_group.view.*
import org.greenrobot.eventbus.EventBus
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.sampling.SamplingUnits
import org.taskforce.episample.config.sampling.subsets.SamplingSubsetViewModel
import org.taskforce.episample.databinding.FragmentConfigSamplingNoGroupBinding


class SamplingNoGroupFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var viewModel: SamplingNoGroupViewModel
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var configBuildViewModel: ConfigBuildViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, SamplingUnits.values().map { if (it == SamplingUnits.PERCENT) it.displayName else configBuildViewModel.configBuildManager.config.enumerationSubject?.plural ?: it.name })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(requireActivity(), SamplingNoGroupViewModelProvider(configBuildViewModel.configBuildManager.config.samplingMethod.units, configBuildViewModel.configBuildManager.config.samplingMethod.id)).get(SamplingNoGroupViewModel::class.java)

        val binding = DataBindingUtil.inflate<FragmentConfigSamplingNoGroupBinding>(inflater, R.layout.fragment_config_sampling_no_group, container, false)
        binding.vm = viewModel
        binding.root.sampleUnitSpinner.adapter = spinnerAdapter
        binding.root.sampleUnitSpinner.setSelection(SamplingUnits.values().indexOf(configBuildViewModel.configBuildManager.config.samplingMethod.units), false)
        binding.root.sampleUnitSpinner.onItemSelectedListener = this
        return binding.root
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //NOP
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.samplingUnit = SamplingUnits.values()[position]
        EventBus.getDefault().post(SamplingSubsetViewModel.Event.SamplingUnitsChanged(viewModel.samplingUnit))
    }

    companion object {
        //TODO look at this
        const val HELP_TARGET = "#TODO"
    }
}
