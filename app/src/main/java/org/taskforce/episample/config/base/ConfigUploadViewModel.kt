package org.taskforce.episample.config.base

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService

class ConfigUploadViewModel(
        languageService: LanguageService,
        private val stepper: Stepper) : ViewModel(), StepperCallback {

    init {
        languageService.update = {
            skipText.set(it.getString(R.string.config_upload_skip))
        }
    }

    val skipText = ObservableField(languageService.getString(R.string.config_upload_skip))

    fun skip() {
        stepper.next()
    }

    override fun onNext(): Boolean {
        return true
    }

    override fun onBack(): Boolean {
        return true
    }

    override fun enableNext() = true

    override fun enableBack() = true
}