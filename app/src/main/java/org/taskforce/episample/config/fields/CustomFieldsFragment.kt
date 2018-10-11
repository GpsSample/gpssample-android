package org.taskforce.episample.config.fields

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.base.ConfigManager
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigFieldsBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class CustomFieldsFragment : Fragment() {

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var configBuildViewModel: ConfigBuildViewModel
    lateinit var viewModel: CustomFieldsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)

        viewModel = ViewModelProviders.of(this@CustomFieldsFragment.requireActivity(), CustomFieldsViewModelFactory(LanguageService(languageManager),
                createNewField,
                configBuildViewModel.configBuildManager))
                .get(CustomFieldsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentConfigFieldsBinding.inflate(inflater).apply {
            headerVm = ConfigHeaderViewModel(
                    LanguageService(languageManager),
                    R.string.config_fields_title,
                    R.string.config_fields_explanation)
            
            vm = viewModel
        }
        
        viewModel.createNewField = createNewField
        
        return binding.root
    }
    
    private val createNewField: () -> Unit = {
        (requireContext() as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .add(R.id.configFrame, CustomFieldsAddFragment.newInstance())
                .addToBackStack(CustomFieldsAddFragment::class.java.name)
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_up)
                .commit()
    }

    companion object {
        const val HELP_TARGET = "#customFields"
    }
}