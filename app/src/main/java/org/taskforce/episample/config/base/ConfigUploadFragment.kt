package org.taskforce.episample.config.base

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferFileBucket
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.config.transfer.TransferViewModel
import org.taskforce.episample.databinding.FragmentConfigUploadBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class ConfigUploadFragment : Fragment() {

    @Inject
    lateinit var transferManager: TransferManager

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigUploadBinding.inflate(inflater).apply {
                val parentViewModel = (parentFragment as ConfigFragment).viewModel
                vm = ViewModelProviders.of(this@ConfigUploadFragment.requireActivity(), ConfigUploadViewModelFactory(LanguageService(languageManager),
                        parentViewModel))
                        .get(ConfigUploadViewModel::class.java)

                parentViewModel.addCallback(this@ConfigUploadFragment.javaClass, vm!!)

                headerVm = ConfigHeaderViewModel(
                        LanguageService(languageManager),
                        R.string.config_upload_title,
                        R.string.config_upload_files_explanation)
                transferVm = TransferViewModel(
                        LanguageService(languageManager),
                        transferManager,
                        requireFragmentManager(),
                        TransferFileBucket.CONFIG)
            }.root

    companion object {
        const val HELP_TARGET = "#configUpload"
    }
}