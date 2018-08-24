package org.taskforce.episample.config.settings.server

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_config_server_settings.view.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigFragment
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigServerSettingsBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class ServerSettingsFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var configBuildViewModel: ConfigBuildViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigServerSettingsBinding.inflate(inflater).apply {
                val parentViewModel = (parentFragment as ConfigFragment).viewModel
                val viewModel = ViewModelProviders.of(this@ServerSettingsFragment.requireActivity(), ServerSettingsViewModelFactory(LanguageService(languageManager),
                        parentViewModel,
                        configBuildViewModel.configBuildManager,
                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            add(languageManager.getString(R.string.server_options))
                        },
                        {
                            view?.serverSelectDropdown?.selectedItemPosition
                        },
                        arrayOf(languageManager.getString(R.string.server_options))))
                        .get(ServerSettingsViewModel::class.java)

                parentViewModel.addCallback(this@ServerSettingsFragment.javaClass, viewModel)
                vm = viewModel
                headerVm = ConfigHeaderViewModel(LanguageService(languageManager),
                        R.string.config_server_auth_title, R.string.config_server_auth_description)
            }.root

    companion object {
        const val HELP_TARGET = "#serverSettings"
    }
}