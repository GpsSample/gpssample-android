package org.taskforce.episample.config.survey

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigSurveyBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class SurveyExportFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var configBuildViewModel: ConfigBuildViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigSurveyBinding.inflate(inflater).apply {
                headerVm = ConfigHeaderViewModel(
                        LanguageService(languageManager),
                        R.string.config_survey_select_title, R.string.config_survey_select_description)
                vm = ViewModelProviders.of(this@SurveyExportFragment.requireActivity(), SurveyExportViewModelFactory(
                        LanguageService(languageManager),
                        configBuildViewModel.configBuildManager))
                        .get(SurveyExportViewModel::class.java)
                        
            }.root

    companion object {
        const val HELP_TARGET = "#surveyExport"
    }
}