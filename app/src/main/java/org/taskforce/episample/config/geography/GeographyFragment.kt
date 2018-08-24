package org.taskforce.episample.config.geography

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigFragment

import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferFileBucket
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.config.transfer.TransferViewModel
import org.taskforce.episample.databinding.FragmentConfigGeographyBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class GeographyFragment : Fragment() {

    @Inject
    lateinit var transferManager: TransferManager

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var viewModel: GeographyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentConfigGeographyBinding.inflate(inflater).apply {
            viewModel = ViewModelProviders.of(this@GeographyFragment.requireActivity(), GeographyViewModelFactory(LanguageService(languageManager),
                    (parentFragment as ConfigFragment).viewModel,
                    EnumerationAreaAdapter()))
                    .get(GeographyViewModel::class.java)
            
            /**
             * When the quickstart block was included in the GeographyViewModelFactory, it was only
             * being created on the first instance of the Fragment. If you navigated away from the Fragment
             * and came back, a new Fragment instance was created but the ViewModel was returned the same
             * Singleton that still referenced the childFragmentManager from the first instance of
             * the Fragment. It needs to be reset every time you get the ViewModel.
             */
            viewModel.quickstart = showQuickStartDialog()

            (parentFragment as ConfigFragment)
                    .viewModel.addCallback(this@GeographyFragment.javaClass, viewModel)

            headerVm = ConfigHeaderViewModel(
                    LanguageService(languageManager),
                    R.string.config_geography_title,
                    R.string.config_geography_explanation)
            vm = viewModel
            transferVm = TransferViewModel(
                    LanguageService(languageManager),
                    transferManager,
                    childFragmentManager,
                    TransferFileBucket.ENUMERATION)
        }
        
        return binding.root
    }
    
    private fun showQuickStartDialog() = {
        GeographyDialogFragment().show(childFragmentManager, GeographyDialogFragment::class.java.simpleName)
    }

    companion object {
        const val HELP_TARGET = "#geography"
    }
}