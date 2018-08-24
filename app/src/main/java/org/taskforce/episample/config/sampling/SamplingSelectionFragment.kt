package org.taskforce.episample.config.sampling

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_config_sampling.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigFragment
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigSamplingBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class SamplingSelectionFragment : Fragment(), SamplingDropdownProvider {

    @Inject
    lateinit var languageManager: LanguageManager

    override lateinit var samplingSelectionInputTypeAdapter: ArrayAdapter<String>

    override lateinit var samplingSelectionTypeAdapter: ArrayAdapter<String>

    lateinit var configBuildViewModel: ConfigBuildViewModel
    lateinit var samplingSelectionViewModel: SamplingSelectionViewModel
    lateinit var languageService: LanguageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)

        languageService = LanguageService(languageManager)
        languageService.update = {
            samplingSelectionViewModel.samplingMethodTitle.notifyChange()
            samplingSelectionViewModel.samplingMethodHint.notifyChange()
        }
        lifecycle.addObserver(languageService)

        samplingSelectionInputTypeAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            addAll(SamplingSelectionInputType.values().map {
                it.name.toLowerCase().capitalize()
            })
        }

        samplingSelectionTypeAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            addAll(SamplingSelectionType.values().map {
                it.displayText
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentConfigSamplingBinding>(inflater, R.layout.fragment_config_sampling, container, false)

        binding.headerVm = ConfigHeaderViewModel(
                LanguageService(languageManager),
                R.string.config_sampling_title, R.string.config_sampling_explanation)

        val parentViewModel = (parentFragment as ConfigFragment).viewModel

        samplingSelectionViewModel = ViewModelProviders.of(requireActivity(), SamplingSelectionViewModelFactory(
                SamplingSelectionType.SIMPLE_RANDOM_SAMPLE,
                SamplingSelectionInputType.PERCENT,
                parentViewModel,
                configBuildViewModel.configBuildManager,
                this))
                .get(SamplingSelectionViewModel::class.java)

        parentViewModel.addCallback(javaClass, samplingSelectionViewModel)

        binding.vm = samplingSelectionViewModel
        binding.languageService = languageService

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        Handler().post {

            val inputTypeIndex = SamplingSelectionInputType.values().indexOf(samplingSelectionViewModel.selectionInputType.get()!!)
            sampleMethodInputTypeSelector.setSelection(inputTypeIndex)

            val typeIndex = SamplingSelectionType.values().indexOf(samplingSelectionViewModel.selectionType.get()!!)
            sampleMethodTypeSelector.setSelection(typeIndex)

            sampleMethodTypeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // no-op
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    samplingSelectionViewModel.selectionType.set(SamplingSelectionType.values()[position])
                }
            }

            sampleMethodInputTypeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // no-op
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    samplingSelectionViewModel.selectionInputType.set(SamplingSelectionInputType.values()[position])
                }
            }
        }
    }

    companion object {
        const val HELP_TARGET = "#samplingSelection"
    }
}

interface SamplingSelectionOnDatasetChanged {
    fun onSamplingSelectionTypeChanged(type: SamplingSelectionType)
    fun onSamplingSelectionInputChanged(input: SamplingSelectionInputType)
}