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
import org.taskforce.episample.databinding.FragmentSamplesBinding
import javax.inject.Inject


class SamplesFragment : Fragment() {
    @Inject
    lateinit var config: Config
    @Inject
    lateinit var collectManager: CollectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).collectComponent?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentSamplesBinding>(inflater, R.layout.fragment_samples, container, false)
        binding.vm = ViewModelProviders.of(this, SamplesFragmentViewModelFactory(resources, config.enumerationSubject, collectManager, config.displaySettings))
                .get(SamplesFragmentViewModel::class.java)
        binding.setLifecycleOwner(this)
        return binding.root
    }
}