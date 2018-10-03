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
import org.taskforce.episample.R
import org.taskforce.episample.databinding.FragmentConfigSamplingNoGroupBinding


class SamplingNoGroupFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var viewModel: SamplingNoGroupViewModel
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, SamplingUnit.values().map { it.displayName })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(requireActivity(), SamplingNoGroupViewModelProvider()).get(SamplingNoGroupViewModel::class.java)

        val binding = DataBindingUtil.inflate<FragmentConfigSamplingNoGroupBinding>(inflater, R.layout.fragment_config_sampling_no_group, container, false)
        binding.vm = viewModel
        binding.root.sampleUnitSpinner.adapter = spinnerAdapter
        binding.root.sampleUnitSpinner.onItemSelectedListener = this
        return binding.root
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //NOP
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.samplingUnit = SamplingUnit.values()[position]
    }

    companion object {
        //TODO look at this
        const val HELP_TARGET = "#TODO"
    }
}

enum class SamplingUnit(val displayName: String) {
    HOUSEHOLDS("Households"),
    PERCENT("Percent")
}