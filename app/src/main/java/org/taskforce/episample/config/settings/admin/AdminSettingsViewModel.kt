package org.taskforce.episample.config.settings.admin

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.support.v4.app.FragmentActivity
import android.view.View
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.ConfigSuccessActivity
import org.taskforce.episample.toolbar.managers.LanguageManager

class AdminSettingsViewModel(
        private val configBuildManager: ConfigBuildManager) : ViewModel(), BaseConfigViewModel {
    val password = object : ObservableField<String>("") {
        override fun set(value: String?) {
            super.set(value)

            error.set(validatePassword(value))
        }
    }
    val isValid = object : ObservableField<Boolean>(password) {
        override fun get() = validatePassword(password.get()) == LanguageManager.undefinedStringResourceId
    }
    val hint = ObservableField(R.string.config_admin_password)
    var error = ObservableField(LanguageManager.undefinedStringResourceId)
    val errorEnabled = ObservableField(false)

    private fun validatePassword(password: String?): Int {
        if (password.isNullOrEmpty()) {
            return R.string.config_password_error_blank
        }

        return LanguageManager.undefinedStringResourceId
    }

    override val nextText: String
        get() = "DONE"
    override val progress: Int
        get() = 8
    override val backEnabled: ObservableField<Boolean> = ObservableField(true)
    override val nextEnabled: ObservableField<Boolean> = isValid

    override fun onNextClicked(view: View) {
        configBuildManager.setAdminSettings(AdminSettings(password.get()!!))

        ConfigSuccessActivity.startActivity(view.context, configBuildManager.config)
        (view.context as FragmentActivity).finish()
    }

    override fun onBackClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
        fragmentManager.popBackStack()
    }
}