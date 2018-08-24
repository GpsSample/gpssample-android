package org.taskforce.episample.config.landmark

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
import org.taskforce.episample.config.base.ConfigManager
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferFileBucket
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.config.transfer.TransferViewModel
import org.taskforce.episample.databinding.FragmentConfigLandmarksBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class LandmarkFragment : Fragment() {

    @Inject
    lateinit var landmarkTypeManager: LandmarkTypeManager

    @Inject
    lateinit var transferManager: TransferManager

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var configBuildViewModel: ConfigBuildViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigLandmarksBinding.inflate(inflater).apply {
                headerVm = ConfigHeaderViewModel(
                        LanguageService(languageManager),
                        R.string.config_landmarks_title, R.string.config_landmarks_explanation)
                val parentModel = (parentFragment as ConfigFragment).viewModel
                val viewModel = ViewModelProviders.of(this@LandmarkFragment.requireActivity(), LandmarkViewModelFactory(
                        LanguageService(languageManager),
                        parentModel, {
                    requireActivity().supportFragmentManager.beginTransaction()
                            .add(R.id.configFrame, LandmarkAddFragment(), LandmarkAddFragment::class.java.name)
                            .addToBackStack(LandmarkAddFragment::class.java.name)
                            .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_up)
                            .commit()
                }, {
                    configBuildViewModel.configBuildManager.setLandmarkTypes(landmarkTypeManager.landmarks)
                }, {
                    landmarkTypeManager.removeLandmarkType(it)
                },{
                    val fragment = LandmarkAddFragment()
                    fragment?.landmarkTypeToEdit = it

                    activity!!.supportFragmentManager.beginTransaction()
                            .add(R.id.configFrame, fragment, LandmarkAddFragment::class.java.name)
                            .addToBackStack(LandmarkAddFragment::class.java.name)
                            .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_up)
                            .commit()
                },
                        landmarkTypeManager.landmarksObservable))
                        .get(LandmarkViewModel::class.java)


                parentModel.addCallback(this@LandmarkFragment.javaClass, viewModel)
                vm = viewModel
                transferVm = TransferViewModel(
                        LanguageService(languageManager),
                        transferManager,
                        requireFragmentManager(), TransferFileBucket.LANDMARK)
            }.root

    companion object {
        const val HELP_TARGET = "#landmarks"
    }
}