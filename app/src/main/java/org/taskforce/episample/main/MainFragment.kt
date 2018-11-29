package org.taskforce.episample.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_main.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.ui.CollectFragment
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentMainBinding
import org.taskforce.episample.db.navigation.ResolvedNavigationPlan
import org.taskforce.episample.help.HelpUtil
import org.taskforce.episample.mapbox.MapboxDownloadFragment
import org.taskforce.episample.navigation.ui.NavigationActivity
import org.taskforce.episample.sampling.ui.SamplingFragment
import org.taskforce.episample.supervisor.upload.ui.StudyUploadFragment
import org.taskforce.episample.sync.managers.SyncManager
import org.taskforce.episample.sync.ui.SyncActivity
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import javax.inject.Inject

class MainFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var syncManager: SyncManager

    lateinit var viewModel: MainViewModel

    private var navigationPlans = listOf<ResolvedNavigationPlan>()
    private val navigationPlanObserver = Observer<List<ResolvedNavigationPlan>> {
        it?.let {
            navigationPlans = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        viewModel = ViewModelProviders.of(this, MainViewModelFactory(
                requireActivity().application,
                LanguageService(languageManager),
                syncManager.lastSynced,
                collectOnClick = {
                    requireFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainFrame, CollectFragment.newInstance())
                            .addToBackStack(CollectFragment::class.java.name)
                            .commit()
                },
                navigateOnClick = {
                    if (navigationPlans.isNotEmpty() == true) {
                        NavigationActivity.startActivity(requireContext())
                    } else {
                        Toast.makeText(requireContext(), "No navigation plans created.", Toast.LENGTH_SHORT).show()
                    }
                },
                syncOnClick = {
                    val isSupervisor = viewModel.userSession.isSupervisor
                    SyncActivity.startActivity(requireContext(), isSupervisor)
                },
                sampleOnClick = {
                    requireFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainFrame, SamplingFragment())
                            .addToBackStack(SamplingFragment::class.java.name)
                            .commit()
                },
                finalOnClick = {
                    requireFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainFrame, StudyUploadFragment.newInstance())
                            .addToBackStack(StudyUploadFragment::class.java.name)
                            .commit()
                },
                mapOnClick = {
                    requireFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainFrame, MapboxDownloadFragment.newInstance())
                            .addToBackStack(MapboxDownloadFragment::class.java.name)
                            .commit()
                }
        )).get(MainViewModel::class.java)

        viewModel.studyRepository.getNavigationPlans().observe(this, navigationPlanObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentMainBinding.inflate(inflater).apply {
                vm = viewModel
                setLifecycleOwner(this@MainFragment)
                // TODO set title to studyname
                toolbarVm = ToolbarViewModel(
                        LanguageService(languageManager),
                        languageManager,
                        HELP_TARGET)

            }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.studyTitle.observe(this@MainFragment, Observer {
            toolbar?.title = it ?: LanguageService(languageManager).getString(R.string.app_name)
        })

        fragment_main_toolbarBackButton.setOnClickListener({
            requireActivity().finish()
        })

        fragment_main_toolbarHelp.setOnClickListener({
            HelpUtil.startHelpActivity(requireContext())
        })

        fragment_main_toolbarLanguage.setOnClickListener({
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.unable_to_open_language_preferences, Toast.LENGTH_SHORT).show()
            }

        })
    }

    companion object {
        const val HELP_TARGET = "#mainScreen"

        fun newInstance(): Fragment {
            return MainFragment()
        }
    }

}