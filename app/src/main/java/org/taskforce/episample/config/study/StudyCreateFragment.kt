package org.taskforce.episample.config.study

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.auth.LoginActivity
import org.taskforce.episample.config.base.ConfigHeaderViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferFileBucket
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.config.transfer.TransferViewModel
import org.taskforce.episample.databinding.FragmentStudyCreateBinding
import org.taskforce.episample.db.ConfigRoomDatabase
import org.taskforce.episample.injection.CollectModule
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import org.taskforce.episample.utils.getCompatColor
import org.taskforce.episample.utils.inflater
import javax.inject.Inject

class StudyCreateFragment : Fragment() {

    @Inject
    lateinit var transferManager: TransferManager

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var configId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)

        configId = arguments!!.getString(ARG_CONFIG_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentStudyCreateBinding.inflate(inflater.context.inflater).apply {
                setLifecycleOwner(this@StudyCreateFragment)
                headerVm = ConfigHeaderViewModel(
                        LanguageService(languageManager),
                        R.string.study_create_title, R.string.study_create_description)
                arguments?.let {
                    vm = ViewModelProviders.of(this@StudyCreateFragment, StudyCreateViewModelFactory(
                            requireActivity().application,
                            LanguageService(languageManager),
                            requireContext().getCompatColor(R.color.colorAccent),
                            requireContext().getCompatColor(R.color.textColorDisabled),
                            { configId, studyId ->
                                LoginActivity.startActivity(requireContext(), configId, studyId)
                                requireActivity().finish()
                            },
                            configId,
                            false))
                            .get(StudyCreateViewModel::class.java)
                }
                transferVm = TransferViewModel(
                        LanguageService(languageManager),
                        transferManager,
                        requireFragmentManager(),
                        TransferFileBucket.MAP_LAYER)
                toolbarVm = ToolbarViewModel(
                        LanguageService(languageManager),
                        languageManager,
                        HELP_TARGET, {
                    requireActivity().supportFragmentManager.popBackStack()
                }).apply {
                    title = languageManager.getString(R.string.study_create_header)
                }
            }.root

    companion object {
        const val HELP_TARGET = "#studyCreate"
        private const val ARG_CONFIG_ID = "EXTRA_CONFIG_ID"

        fun newInstance(configId: String): Fragment {
            val fragment = StudyCreateFragment()
            fragment.arguments = Bundle()
            fragment.arguments!!.putString(ARG_CONFIG_ID, configId)
            return fragment
        }
    }
}