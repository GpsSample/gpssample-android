package org.taskforce.episample.config.sampling

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.databinding.FragmentConfigSamplingBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class SamplingSelectionFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var samplingSelectionInputTypeAdapter: ArrayAdapter<String>
    lateinit var samplingSelectionTypeAdapter: ArrayAdapter<String>

    lateinit var samplingSelectionViewModel: SamplingSelectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)

        samplingSelectionInputTypeAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            addAll(SamplingUnits.values().map {
                it.name.toLowerCase().capitalize()
            })
        }

        samplingSelectionTypeAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            addAll(SamplingMethodology.values().map {
                it.displayText
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentConfigSamplingBinding>(inflater, R.layout.fragment_config_sampling, container, false)

        val configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)
        val config = configBuildViewModel.configBuildManager.config

        when (config.samplingMethod.grouping) {
            SamplingGrouping.SUBSETS -> binding.subsetRadioButton.isChecked = true
            SamplingGrouping.STRATA -> binding.strataRadioButton.isChecked = true
            SamplingGrouping.NONE -> binding.noneRadioButton.isChecked = true
        }

        samplingSelectionViewModel = ViewModelProviders.of(requireActivity(), SamplingSelectionViewModelFactory(
                config.samplingMethod))
                .get(SamplingSelectionViewModel::class.java)


        binding.sampleMethodTypeSelector.adapter = samplingSelectionTypeAdapter
        binding.sampleMethodTypeSelector.setSelection(SamplingMethodology.values().indexOf(config.samplingMethod.type), false)
        binding.sampleMethodTypeSelector.onItemSelectedListener = samplingSelectionViewModel.samplingMethodOnItemSelectedListener

        binding.vm = samplingSelectionViewModel
        samplingSelectionViewModel.eventBus.post(SamplingMethodChanged(config.samplingMethod.type, config.samplingMethod.grouping))

        return binding.root
    }

    companion object {
        const val HELP_TARGET = "#samplingSelection"
    }
}

interface SamplingSelectionOnDatasetChanged {
    fun samplingMethodologyChanged(type: SamplingMethodology)
    fun samplingUnitsChanged(input: SamplingUnits)
}