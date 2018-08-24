package org.taskforce.episample.config.settings.user

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
import org.taskforce.episample.databinding.FragmentConfigUserSettingsBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class UserSettingsFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService

    lateinit var configBuildViewModel: ConfigBuildViewModel
    lateinit var userSettingsViewModel: UserSettingsViewModel

    lateinit var photoCompressionOptions: Array<Pair<Int, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)

        photoCompressionOptions = arrayOf(0 to getString(R.string.photo_options_no_compression), 50 to getString(R.string.photo_options_some_compression), 100 to getString(R.string.photo_options_maximum_compression))
        
        languageService = LanguageService(languageManager)
        languageService.update = {
            userSettingsViewModel.gpsPreferredPrecisionHint.notifyChange()
            userSettingsViewModel.gpsMinimumPrecisionHint.notifyChange()
            userSettingsViewModel.gpsMinimumPrecisionError.notifyChange()
            userSettingsViewModel.gpsExplanation.notifyChange()
            userSettingsViewModel.displacementEnforcementTitle.notifyChange()
            userSettingsViewModel.displacementEnforcementSmallHint.notifyChange()
            userSettingsViewModel.photoEnforcementTitle.notifyChange()
            userSettingsViewModel.requireCommentTitle.notifyChange()
            userSettingsViewModel.requireCommentSmallHint.notifyChange()
            userSettingsViewModel.permissionsTitle.notifyChange()
            userSettingsViewModel.supervisorDisablePhotosTitle.notifyChange()
            userSettingsViewModel.supervisorPassword.notifyChange()

        }
        lifecycle.addObserver(languageService)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigUserSettingsBinding.inflate(inflater).apply {
                userSettingsViewModel = ViewModelProviders.of(this@UserSettingsFragment.requireActivity(), UserSettingsViewModelFactory(
                        (parentFragment as ConfigFragment).viewModel,
                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            addAll(*(configBuildViewModel.configBuildManager.photoCompressionOptions.map {
                                it.second
                            }.toTypedArray()))
                        },
                        {
                            configBuildViewModel.configBuildManager.photoCompressionOptions[photoCompressionSelector.selectedItemPosition].first
                        },
                        configBuildViewModel.configBuildManager
                ))
                        .get(UserSettingsViewModel::class.java)

                (parentFragment as ConfigFragment).viewModel.addCallback(this@UserSettingsFragment.javaClass, userSettingsViewModel)
                headerVm = ConfigHeaderViewModel(LanguageService(languageManager),
                        R.string.config_user_settings_title, R.string.config_user_settings_explanation)
                vm = userSettingsViewModel
                
                languageService = this@UserSettingsFragment.languageService
                
            }.root

    companion object {
        const val HELP_TARGET = "#userSettings"
    }
}