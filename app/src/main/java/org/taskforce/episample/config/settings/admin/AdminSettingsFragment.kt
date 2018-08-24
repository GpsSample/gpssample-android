package org.taskforce.episample.config.settings.admin

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigFragment
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigAdminBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class AdminSettingsFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService

    lateinit var configBuildViewModel: ConfigBuildViewModel
    lateinit var adminSettingsViewModel: AdminSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)

        languageService = LanguageService(languageManager)
        languageService.update = {
            adminSettingsViewModel.hint.notifyChange()
            adminSettingsViewModel.error.notifyChange()
        }
        lifecycle.addObserver(languageService)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigAdminBinding.inflate(inflater).apply {
                headerVm = ConfigHeaderViewModel(
                        LanguageService(languageManager),
                        R.string.config_admin_title, R.string.config_admin_explanation)
                val parentFragment = parentFragment as ConfigFragment
                adminSettingsViewModel = ViewModelProviders.of(this@AdminSettingsFragment.requireActivity(), AdminSettingsViewModelFactory(
                        parentFragment.viewModel, configBuildViewModel.configBuildManager))
                        .get(AdminSettingsViewModel::class.java)

                parentFragment.viewModel.addCallback(this@AdminSettingsFragment.javaClass, adminSettingsViewModel)
                vm = adminSettingsViewModel
                languageService = this@AdminSettingsFragment.languageService
            }.root

    companion object {
        const val HELP_TARGET = "#adminSettings"
    }
}