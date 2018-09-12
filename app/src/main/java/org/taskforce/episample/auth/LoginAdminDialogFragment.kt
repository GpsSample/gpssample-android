package org.taskforce.episample.auth

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
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

    lateinit var configId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        configId = arguments!!.getString(ARG_CONFIG_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentLoginAdminDialogBinding>(inflater, R.layout.fragment_login_admin_dialog, container, false)

        binding.vm = ViewModelProviders.of(this, LoginAdminDialogViewModelFactory(
                requireActivity().application,
                configId,
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
                })).get(LoginAdminDialogViewModel::class.java)

        binding.setLifecycleOwner(this)

        return binding.root
    }

    companion object {
        private const val ARG_CONFIG_ID = "ARG_CONFIG_ID"

        fun newInstance(configId: String): DialogFragment {
            val fragment = LoginAdminDialogFragment()
            fragment.arguments = Bundle()
            fragment.arguments!!.putString(ARG_CONFIG_ID, configId)
            return fragment
        }
    }
}