package org.taskforce.episample.config.settings.display

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigFragment
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigDisplayBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class DisplaySettingsFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var configBuildViewModel: ConfigBuildViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigDisplayBinding.inflate(inflater).apply {
                val viewModel = ViewModelProviders.of(this@DisplaySettingsFragment.requireActivity(), DisplaySettingsViewModelFactory(
                        LanguageService(languageManager),
                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            addAll(languageManager.languages.map {
                                it.value.name
                            })
                        },

                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            addAll("dd/mm/yyyy", "mm/dd/yyyy")
                        },

                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            addAll("12 hour", "24 hour")
                        },
                        configBuildViewModel.configBuildManager))
                        .get(DisplaySettingsViewModel::class.java)

                (parentFragment as ConfigFragment).viewModel.addCallback(this@DisplaySettingsFragment.javaClass, viewModel)
                headerVm = ConfigHeaderViewModel(
                        LanguageService(languageManager),
                        R.string.config_display_title, R.string.config_display_explanation)
                vm = viewModel
            }.root

    companion object {
        const val HELP_TARGET = "#displaySettings"
    }
}