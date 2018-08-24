package org.taskforce.episample.config.settings.server

import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.ObservableField
import android.widget.ArrayAdapter
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.base.StepperCallback
import org.taskforce.episample.config.language.LanguageService

class ServerSettingsViewModel(
        languageService: LanguageService,
        private val stepper: Stepper,
        private val configBuildManager: ConfigBuildManager,
        val serverTypeAdapter: ArrayAdapter<String>,
        private val serverSelectPositionProvider: () -> Int?,
        private val serverOptions: Array<String>) : ViewModel(), StepperCallback {

    private val validityObserver = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            stepper.enableNext(isValid.get()!!, ServerSettingsFragment::class.java)
        }
    }

    val url = ObservableField("")
    val username = ObservableField("")
    val password = ObservableField("")

    val isValid = object: ObservableField<Boolean>(url, username, password) {
        override fun get() = url.get()!!.isNotEmpty() && username.get()!!.isNotEmpty() && password.get()!!.isNotEmpty()
    }

    init {
        languageService.update = {
            selectTitle.set(languageService.getString(R.string.config_server_type))
            urlHint.set(languageService.getString(R.string.config_server_url))
            userHint.set(languageService.getString(R.string.config_server_user))
            passwordHint.set(languageService.getString(R.string.config_server_password))
        }

        isValid.addOnPropertyChangedCallback(validityObserver)
    }

    override fun onCleared() {
        isValid.removeOnPropertyChangedCallback(validityObserver)
        super.onCleared()
    }
    
    val selectTitle = ObservableField(languageService.getString(R.string.config_server_type))

    val urlHint = ObservableField(languageService.getString(R.string.config_server_url))

    val userHint = ObservableField(languageService.getString(R.string.config_server_user))

    val passwordHint = ObservableField(languageService.getString(R.string.config_server_password))

    override fun onNext(): Boolean {
        return if (isValid.get()!!) {
            serverSelectPositionProvider()?.let {
                configBuildManager.setServerSettings(ServerSettings(
                        serverOptions[it],
                        url.get()!!,
                        username.get()!!,
                        password.get()!!
                ))
            }
            true
        } else {
            false
        }
    }

    override fun onBack() = true

    override fun enableNext() = isValid.get()!!

    override fun enableBack() = true
}