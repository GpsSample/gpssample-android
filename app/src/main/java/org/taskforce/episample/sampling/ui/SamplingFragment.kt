package org.taskforce.episample.sampling.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.databinding.FragmentSamplingBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import javax.inject.Inject


class SamplingFragment : Fragment(), Observer<Int> {
    @Inject
    lateinit var collectManager: CollectManager
    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var toolbarViewModel: ToolbarViewModel
    lateinit var languageService: LanguageService
    lateinit var numberOfSamples: LiveData<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).collectComponent?.inject(this)
        languageService = LanguageService(languageManager)
        numberOfSamples = collectManager.getNumberOfSamples()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        toolbarViewModel = ToolbarViewModel(
                languageService,
                languageManager,
                GenerateSampleFragment.HELP_TARGET,
                dismiss)
        toolbarViewModel.title = languageService.getString(R.string.sample)


        val binding = DataBindingUtil.inflate<FragmentSamplingBinding>(inflater, R.layout.fragment_sampling, container, false)
        binding.toolbarVm = toolbarViewModel
        binding.setLifecycleOwner(this)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        numberOfSamples.observe(this, this)
    }

    override fun onPause() {
        super.onPause()
        numberOfSamples.removeObservers(this)
    }

    override fun onChanged(t: Int?) {
        val amount = numberOfSamples.value
        if (amount != null) {
            val fragment = if (amount < 1) {
                GenerateSampleFragment()
            } else {
                SamplesFragment()
            }
            childFragmentManager.beginTransaction()
                    .replace(R.id.samplingContent, fragment)
                    .commit()
        }
    }

    private val dismiss: () -> Unit = {
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
        } else {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}