package org.taskforce.episample.config.name

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.base.ConfigManager
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigNameBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class ConfigNameFragment : Fragment() {

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var configBuildViewModel: ConfigBuildViewModel
    lateinit var languageService: LanguageService
    lateinit var configNameViewModel: ConfigNameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)

        languageService = LanguageService(languageManager)
        languageService.update = {
            configNameViewModel.hint.notifyChange()
            configNameViewModel.error.notifyChange()
        }
        lifecycle.addObserver(languageService)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentConfigNameBinding>(inflater, R.layout.fragment_config_name, container, false)

        configNameViewModel =
                ViewModelProviders.of(requireActivity(), ConfigNameViewModelFactory(requireActivity().application,
                        configBuildViewModel.configBuildManager
                )).get(ConfigNameViewModel::class.java)

        binding.vm = configNameViewModel
        binding.languageService = languageService
        binding.headerVm = ConfigHeaderViewModel(
                LanguageService(languageManager),
                R.string.config_name_title, R.string.config_name_explanation)

        val emojiFilter = InputFilter { source, start, end, dest, destStart, destEnd ->
            for (i in start until end) {
                val type = Character.getType(source?.get(i)!!)
                if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                    return@InputFilter ""
                }
            }
            null
        }

        binding.nameInput.filters = arrayOf(emojiFilter)

        return binding.root
    }

    companion object {
        fun newInstance(): Fragment {
            return ConfigNameFragment()
        }

        const val HELP_TARGET = "#configName"
    }
}