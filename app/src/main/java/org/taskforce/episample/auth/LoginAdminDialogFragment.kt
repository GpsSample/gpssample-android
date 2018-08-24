package org.taskforce.episample.auth

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigActivity
import org.taskforce.episample.databinding.FragmentLoginAdminDialogBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.closeKeyboard
import javax.inject.Inject

class LoginAdminDialogFragment : DialogFragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        password = arguments!!.getString(ARG_PASSWORD)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentLoginAdminDialogBinding.inflate(inflater).apply {
                vm = LoginAdminDialogViewModel(
                        password,
                        languageManager.getString(R.string.login_admin_title),
                        languageManager.getString(R.string.login_admin_hint),
                        languageManager.getString(R.string.cancel),
                        languageManager.getString(R.string.signin),
                        languageManager.getString(R.string.login_invalid_password),
                        {
                            this@LoginAdminDialogFragment.view?.closeKeyboard()
                            this@LoginAdminDialogFragment.dismiss()
                        },
                        {
                            ConfigActivity.startActivity(requireContext())
                            requireActivity().finish()
                        })
            }.root

    companion object {
        private const val ARG_PASSWORD = "PASSWORD"

        fun newInstance(password: String): DialogFragment {
            val fragment = LoginAdminDialogFragment()
            fragment.arguments = Bundle()
            fragment.arguments!!.putString(ARG_PASSWORD, password)
            return fragment
        }
    }
}