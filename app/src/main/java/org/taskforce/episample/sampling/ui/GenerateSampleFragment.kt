package org.taskforce.episample.sampling.ui

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.databinding.FragmentSampleCreateBinding
import javax.inject.Inject

class GenerateSampleFragment : Fragment() {
    @Inject
    lateinit var config: Config
    @Inject
    lateinit var collectManager: CollectManager

    lateinit var viewModel: SamplingGenerationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).collectComponent?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewModel = ViewModelProviders.of(this, SurveyCreateViewModelFactory(resources, config.enumerationSubject, collectManager, config.displaySettings))
                .get(SamplingGenerationViewModel::class.java)

        val binding = DataBindingUtil.inflate<FragmentSampleCreateBinding>(inflater, R.layout.fragment_sample_create, container, false)
        binding.vm = viewModel
        binding.setLifecycleOwner(this)
        return binding.root
    }

    companion object {
        const val HELP_TARGET = "#sampleCreate"
    }
}