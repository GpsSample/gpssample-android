package org.taskforce.episample.config.language

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
import org.taskforce.episample.config.name.LanguageViewModelFactory
import org.taskforce.episample.config.transfer.TransferFileBucket
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.config.transfer.TransferViewModel
import org.taskforce.episample.databinding.FragmentConfigLanguageBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.createSnackbar
import javax.inject.Inject

class LanguageFragment : Fragment(), LanguageViewModel.LanguageErrorCallback {
    @Inject
    lateinit var transferManager: TransferManager

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var configBuildViewModel: ConfigBuildViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigLanguageBinding.inflate(inflater).apply {

                val parentFragment = parentFragment as ConfigFragment
                val viewModel = ViewModelProviders.of(this@LanguageFragment.requireActivity(), LanguageViewModelFactory(
                        languageManager,
                        LanguageService(languageManager),
                        parentFragment.viewModel, 
                        configBuildViewModel.configBuildManager,
                        this@LanguageFragment)).get(LanguageViewModel::class.java)

                parentFragment.viewModel.addCallback(this@LanguageFragment.javaClass, viewModel)

                languageManager.currentLanguagesObservable.subscribe(viewModel.adapter)

                vm = viewModel
                headerVm = ConfigHeaderViewModel(
                        LanguageService(languageManager),
                        R.string.config_lang_title, R.string.config_lang_explanation)
                transferVm = TransferViewModel(
                        LanguageService(languageManager),
                        transferManager,
                        requireFragmentManager(), TransferFileBucket.LANGUAGE)
            }.root

    override fun onNoLanguageSelectedError() {
        view?.createSnackbar(languageManager.getString(R.string.config_lang_error_none), 
                "") {}
    }

    companion object {
        const val HELP_TARGET = "#language"
    }
}