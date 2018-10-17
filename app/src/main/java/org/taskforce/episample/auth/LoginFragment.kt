package org.taskforce.episample.auth

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.LiveUserSession
import org.taskforce.episample.databinding.FragmentLoginBinding
import org.taskforce.episample.db.StudyRoomDatabase
import org.taskforce.episample.injection.CollectModule
import org.taskforce.episample.main.MainActivity
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class LoginFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService

    lateinit var loginViewModel: LoginViewModel

    lateinit var configId: String
    lateinit var studyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configId = arguments!!.getString(ARG_CONFIG_ID)
        studyId = arguments!!.getString(ARG_STUDY_ID)

        (requireActivity().application as EpiApplication).component.inject(this)

        languageService = LanguageService(languageManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentLoginBinding.inflate(inflater).apply {
                setLifecycleOwner(this@LoginFragment)
                loginViewModel = LoginViewModel(
                        requireActivity().application,
                        LanguageService(languageManager),
                        configId,
                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            // TODO use custom languages
                            addAll(LanguageManager.supportedLocales.map { it.displayLanguage })
                        },
                        { name, config ->
                            val application = (requireActivity().application as EpiApplication)
                            application
                                    .createCollectComponent(CollectModule(application,
                                            StudyRoomDatabase.getDatabase(application),
                                            LiveUserSession(username = name,
                                                    isSupervisor = false,
                                                    studyId = studyId,
                                                    configId = configId),
                                            config
                                    ))
                            MainActivity.startActivity(requireContext())
                        },
                        { name, config ->
                            val application = (requireActivity().application as EpiApplication)
                            application
                                    .createCollectComponent(CollectModule(application,
                                            StudyRoomDatabase.getDatabase(application),
                                            LiveUserSession(username = name,
                                                    isSupervisor = true,
                                                    studyId = studyId,
                                                    configId = configId),
                                            config))
                            MainActivity.startActivity(requireContext())
                        },
                        {
                            showAdminLoginDialog()
                        },
                        {
                            Toast.makeText(requireContext(), getString(R.string.invalid_password), Toast.LENGTH_SHORT).show()
                        }
                )
                vm = loginViewModel
            }.root

    override fun onStop() {
        super.onStop()
        loginViewModel.studyRepository.cleanUp()
    }

    private fun showAdminLoginDialog() {
        LoginAdminDialogFragment.newInstance(configId).show(childFragmentManager, LoginAdminDialogFragment::class.java.simpleName)
    }

    companion object {
        private const val ARG_CONFIG_ID = "EXTRA_CONFIG_ID"
        private const val ARG_STUDY_ID = "EXTRA_STUDY_ID"

        fun newInstance(configId: String, studyId: String): Fragment {
            val fragment = LoginFragment()
            fragment.arguments = Bundle()
            fragment.arguments!!.putString(ARG_CONFIG_ID, configId)
            fragment.arguments!!.putString(ARG_STUDY_ID, studyId)
            return fragment
        }
    }
}