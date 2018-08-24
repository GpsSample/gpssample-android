package org.taskforce.episample.config.sampling

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigSamplingSubsetBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class SamplingSubsetFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigSamplingSubsetBinding.inflate(inflater).apply {
                headerVm = ConfigHeaderViewModel(
                        LanguageService(languageManager),
                        R.string.config_sampling_subset_title, R.string.config_sampling_subset_explanation)
            }.root

    companion object {
        const val HELP_TARGET = "#samplingSubset"
    }
}