package org.taskforce.episample.auth

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentLoginBinding
import org.taskforce.episample.main.MainActivity
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class LoginFragment : Fragment() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService

    lateinit var loginViewModel: LoginViewModel
    var adminPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)

        languageService = LanguageService(languageManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentLoginBinding.inflate(inflater).apply {
                loginViewModel = LoginViewModel(
                        requireActivity().application,
                        LanguageService(languageManager),
                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            // TODO use custom languages
                            addAll(LanguageManager.supportedLocales.map { it.displayLanguage })
                        },
                        {
                            authManager.username = it
                            MainActivity.startActivity(requireContext())
                        },
                        { name ->
                                authManager.isSupervisor = true
                                authManager.username = name
                                MainActivity.startActivity(requireContext())
                        },
                        {
                            showAdminLoginDialog()
                        }
                )
                vm = loginViewModel
                loginViewModel.adminPasswordData.observe(this@LoginFragment, Observer {
                    adminPassword = it
                })
            }.root

    private fun showAdminLoginDialog() {
        LoginAdminDialogFragment.newInstance(adminPassword!!).show(childFragmentManager, LoginAdminDialogFragment::class.java.simpleName)
    }

    companion object {
        private const val ARG_CONFIG_ID = "ARG_CONFIG_ID"
        private const val ARG_STUDY_ID = "ARG_STUDY_ID"

        fun newInstance(): Fragment {
            return LoginFragment()
        }
    }
}