package org.taskforce.episample.config.base

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import org.taskforce.episample.utils.closeKeyboard
import org.taskforce.episample.utils.getCompatColor
import javax.inject.Inject

class ConfigFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var configManager: ConfigManager

    lateinit var viewModel: ConfigViewModel

    lateinit var configBuildViewModel: ConfigBuildViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigBinding.inflate(inflater).apply {
                val adapter = ConfigAdapter(childFragmentManager)
                viewModel = ConfigViewModel(
                        configBuildViewModel.configBuildManager,
                        LanguageService(languageManager),
                        requireActivity().getCompatColor(R.color.textColorPrimary),
                        requireActivity().getCompatColor(R.color.textColorDisabled),
                        {
                            activity?.currentFocus?.closeKeyboard()
                        },
                        {
                            ConfigSuccessActivity.startActivity(this@ConfigFragment.requireContext(), configBuildViewModel.configBuildManager.config)
                            this@ConfigFragment.requireActivity().finish()
                        },
                        contentPager,
                        adapter)

                vm = viewModel
                toolbarVm = ToolbarViewModel(
                        LanguageService(languageManager),
                        languageManager,
                        HELP_TARGET)
                        .apply {
                            title = languageManager.getString(R.string.config_toolbar_title)
                            viewModel.stepperStateObservable.subscribe {
                                HELP_TARGET = ConfigAdapter.configFragmentHelpMap[it - 1]!!
                            }
                            languageManager.languageEventObservable.subscribe {
                                title = languageManager.getString(R.string.config_toolbar_title)
                            }
                        }
            }.root

    companion object {
        private const val HELP_TARGET = "#config"

        fun newInstance() = ConfigFragment()
    }
}