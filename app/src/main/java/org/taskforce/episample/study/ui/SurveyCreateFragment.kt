package org.taskforce.episample.study.ui

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentSampleCreateBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import javax.inject.Inject

class SurveyCreateFragment : Fragment() {
    
    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService
    
    lateinit var viewModel: SurveyCreateViewModel
    lateinit var toolbarViewModel: ToolbarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        (requireActivity().application as EpiApplication).collectComponent?.inject(this)
        
        languageService = LanguageService(languageManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, SurveyCreateViewModelFactory(requireActivity().application, dismiss))
                .get(SurveyCreateViewModel::class.java)

        toolbarViewModel = ToolbarViewModel(
                languageService,
                languageManager,
                HELP_TARGET,
                dismiss)
        toolbarViewModel.title = languageService.getString(R.string.sample)


        val binding= DataBindingUtil.inflate<FragmentSampleCreateBinding>(inflater, R.layout.fragment_sample_create, container, false)
        binding.vm = viewModel
        binding.toolbarVm = toolbarViewModel
        
        return binding.root
    }
    
    private val dismiss: () -> Unit = {
        requireActivity().supportFragmentManager.popBackStack()
    }
    
    companion object {
        const val HELP_TARGET = "#sampleCreate"
    }
}