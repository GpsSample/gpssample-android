package org.taskforce.episample.config.settings.admin

import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.ObservableField
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.base.StepperCallback
import org.taskforce.episample.toolbar.managers.LanguageManager

class AdminSettingsViewModel(
        private val stepper: Stepper,
        private val configBuildManager: ConfigBuildManager) : ViewModel(), StepperCallback {

    val password = object: ObservableField<String>("") {
        override fun set(value: String?) {
            super.set(value)
            
            error.set(validatePassword(value))
        }
    }

    val isValid = object: ObservableField<Boolean>(password) {
        override fun get() = validatePassword(password.get()) == LanguageManager.undefinedStringResourceId
    }

    private val validityObserver = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            stepper.enableNext(isValid.get()!!, AdminSettingsFragment::class.java)
        }
    }

    init {
        isValid.addOnPropertyChangedCallback(validityObserver)
    }

    override fun onCleared() {
        isValid.removeOnPropertyChangedCallback(validityObserver)
        super.onCleared()
    }

    val hint = ObservableField(R.string.config_admin_password)

    private fun validatePassword(password: String?): Int {
        if(password.isNullOrEmpty()) {
            return R.string.config_password_error_blank
        }
        
        return LanguageManager.undefinedStringResourceId
    }
    
    var error = ObservableField(LanguageManager.undefinedStringResourceId)

    val errorEnabled = ObservableField(false)

    override fun enableNext() = isValid.get()!!

    override fun enableBack() = true

    override fun onNext(): Boolean {
        return if (!errorEnabled.get()!!) {
            configBuildManager.setAdminSettings(AdminSettings(password.get()!!))
            true
        } else {
            false
        }
    }

    override fun onBack() = true
}