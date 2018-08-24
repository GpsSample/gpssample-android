package org.taskforce.episample.config.base

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.auth.LoginActivity
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.databinding.FragmentConfigStartBinding
import org.taskforce.episample.injection.CollectModule
import org.taskforce.episample.sync.core.DirectTransferService
import org.taskforce.episample.sync.ui.ReceiverSyncStatusViewModel
import org.taskforce.episample.sync.ui.ReceiverSyncStatusViewModelFactory
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import javax.inject.Inject

class ConfigStartFragment : Fragment() {

    @Inject
    lateinit var transferManager: TransferManager

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var directTransferService: DirectTransferService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigStartBinding.inflate(inflater).apply {
                setLifecycleOwner(this@ConfigStartFragment)
                languageService = LanguageService(languageManager)

                vm = ViewModelProviders.of(this@ConfigStartFragment, ConfigStartViewModelFactory(
                        requireActivity().application,
                        {
                            BuildConfigActivity.startActivity(this@ConfigStartFragment.requireContext())
                        },
                        {
                            requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.configFrame, ConfigAllFragment())
                                    .addToBackStack(ConfigAllFragment::class.java.name)
                                    .commit()
                        },
                        {
                            //TODO: Open edit study screen.
                            Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
                        },
                        { config, studyId ->
                            (requireActivity().application as EpiApplication)
                                    .createCollectComponent(CollectModule(requireActivity().application,
                                            config.id, studyId))
                            LoginActivity.startActivity(requireContext())
                            requireActivity().finish()
                        },
                        transferManager
                )).get(ConfigStartViewModel::class.java)

                syncVm = ViewModelProviders.of(this@ConfigStartFragment, ReceiverSyncStatusViewModelFactory(LanguageService(languageManager), {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                })).get(ReceiverSyncStatusViewModel::class.java)
                        .apply {
                            directTransferService = DirectTransferService(this).apply {
                                registerReceiver(requireActivity())
                            }
                }

                toolbarVm = ToolbarViewModel(
                        LanguageService(languageManager),
                        languageManager,
                        HELP_TARGET)
            }.root

    override fun onDestroy() {
        super.onDestroy()
        directTransferService.unregisterReceiver(requireActivity())
    }
    
    companion object {
        private const val HELP_TARGET = "#configSelect"
    }
}