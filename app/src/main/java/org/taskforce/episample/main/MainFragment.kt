package org.taskforce.episample.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.ui.CollectFragment
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentMainBinding
import org.taskforce.episample.navigation.ui.NavigationActivity
import org.taskforce.episample.sync.managers.SyncManager
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import javax.inject.Inject

class MainFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var syncManager: SyncManager

    lateinit var viewModel: MainViewModel

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
                    // TODO remove demo navigation plan creation
                    viewModel.studyRepository.getNavigationPlans().observe(this, Observer {
                        if (it?.isNotEmpty() == true) {
                            NavigationActivity.startActivity(requireContext(), it.first().id)
                        } else {
                            val studyId = viewModel.userSession.studyId
                            viewModel.studyRepository.createDemoNavigationPlan(studyId, { navigationPlanId ->
                                NavigationActivity.startActivity(requireContext(), navigationPlanId)
                            })
                        }
                    })
                },
                syncOnClick = {
                    // TODO open sync
                    Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
//                    requireFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.mainFrame, SyncFragment())
//                            .addToBackStack(SyncFragment::class.java.name)
//                            .commit()
                },
                sampleOnClick = {
                    // TODO open sample
                    Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
//                    requireFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.mainFrame, SurveyCreateFragment())
//                            .addToBackStack(SurveyCreateFragment::class.java.name)
//                            .commit()
                },
                finalOnClick = {
                    // TODO open final
                    Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
//                    requireFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.mainFrame, StudyUploadFragment())
//                            .addToBackStack(StudyUploadFragment::class.java.name)
//                            .commit()
                }
        )).get(MainViewModel::class.java)
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
                viewModel.studyTitle.observe(this@MainFragment, Observer {
                    toolbarVm!!.title = it ?: LanguageService(languageManager).getString(R.string.app_name)
                })

            }.root

    companion object {
        const val HELP_TARGET = "#mainScreen"

        fun newInstance(): Fragment {
            return MainFragment()
        }
    }

}