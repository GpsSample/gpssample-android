package org.taskforce.episample.config.base

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.study.StudyCreateFragment
import org.taskforce.episample.databinding.FragmentConfigSuccessBinding
import org.taskforce.episample.db.config.Study
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import org.taskforce.episample.utils.getCompatColor
import javax.inject.Inject

class ConfigSuccessFragment : Fragment() {

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var config: Config
    lateinit var viewModel: ConfigSuccessViewModel

    private var study: Study? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = arguments!!.getSerializable(ARG_CONFIG) as Config

        (requireActivity().application as EpiApplication).component.inject(this)

        viewModel = ViewModelProviders.of(this, ConfigSuccessViewModelFactory(
                requireActivity().application,
                LanguageService(languageManager),
                {
                    requireActivity().finish()
                },
                {
                    createNewStudy(it)
                },
                configManager,
                config,
                requireContext().getCompatColor(R.color.textColorDisabled),
                requireContext().getCompatColor(R.color.colorAccent),
                requireContext().getCompatColor(R.color.colorError)
        )).get(ConfigSuccessViewModel::class.java)
        viewModel.study.observe(this, Observer {
            study = it
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentConfigSuccessBinding>(inflater, R.layout.fragment_config_success, container, false)

        binding.setLifecycleOwner(this)
        binding.vm = viewModel
        binding.toolbarVm = ToolbarViewModel(
                LanguageService(languageManager),
                languageManager,
                HELP_TARGET).apply {
            title = languageManager.getString(R.string.config_saved)
        }
        binding.headerVm = ConfigHeaderViewModel(
                LanguageService(languageManager),
                R.string.config_success_title,
                R.string.config_success_description)


        return binding.root
    }

    fun createNewStudy(configId: String) {
        if (study == null) {
            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.configFrame, StudyCreateFragment.newInstance(configId))
                    .addToBackStack(StudyCreateFragment::class.java.name)
                    .commit()
        } else {
            // TODO get design advice on UX
            Toast.makeText(requireContext(), "Cannot create more than one study", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        private const val HELP_TARGET = "#configSuccess"
        private const val ARG_CONFIG = "ARG_CONFIG"

        fun newInstance(config: Config): Fragment {
            val fragment = ConfigSuccessFragment()
            fragment.arguments = Bundle()
            fragment.arguments!!.putSerializable(ARG_CONFIG, config)
            return fragment
        }
    }
}