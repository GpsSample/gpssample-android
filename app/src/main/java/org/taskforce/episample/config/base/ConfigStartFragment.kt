package org.taskforce.episample.config.base

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_config_start.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.auth.LoginActivity
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.databinding.FragmentConfigStartBinding
import org.taskforce.episample.help.HelpUtil
import org.taskforce.episample.sync.ui.ReceiverSyncStatusViewModel
import org.taskforce.episample.sync.ui.ReceiverSyncStatusViewModelFactory
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject


class ConfigStartFragment : Fragment() {

    @Inject
    lateinit var transferManager: TransferManager

    @Inject
    lateinit var languageManager: LanguageManager

    private lateinit var syncStatusViewModel: ReceiverSyncStatusViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)

        syncStatusViewModel = ViewModelProviders.of(this@ConfigStartFragment, ReceiverSyncStatusViewModelFactory(requireActivity().application,
                LanguageService(languageManager), {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        })).get(ReceiverSyncStatusViewModel::class.java)
        lifecycle.addObserver(syncStatusViewModel.directTransferService)
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
                        { config, studyId ->
                            LoginActivity.startActivity(requireContext(), config.id, studyId)
                            requireActivity().finish()
                        },
                        transferManager
                )).get(ConfigStartViewModel::class.java)

                syncVm = syncStatusViewModel
                setLifecycleOwner(this@ConfigStartFragment)
            }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_config_start_toolbarHelp.setOnClickListener({
            HelpUtil.startHelpActivity(requireContext())
        })

        fragment_config_start_toolbarLanguage.setOnClickListener({
            val intent = Intent(android.provider.Settings.ACTION_LOCALE_SETTINGS)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.unable_to_open_language_preferences, Toast.LENGTH_SHORT).show()
            }

        })
    }
}