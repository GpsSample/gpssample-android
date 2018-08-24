package org.taskforce.episample.config.language

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.base.StepperCallback
import org.taskforce.episample.toolbar.managers.LanguageManager

class LanguageViewModel(
        languageManager: LanguageManager,
        languageService: LanguageService,
        private val stepper: Stepper,
        private val configBuildManager: ConfigBuildManager,
        private val errorCallback: LanguageErrorCallback) : ViewModel(), StepperCallback {

    val languageListHeader = ObservableField(languageService.getString(R.string.config_lang_list_title))

    var adapter = LanguageAdapter(stepper, languageManager)

    init {
        languageService.update = {
            languageListHeader.set(languageService.getString(R.string.config_lang_list_title))
        }
    }


    override fun onNext(): Boolean {
        return if (adapter.anyChecked) {
            configBuildManager.languages = adapter.checkedLanguages
            true
        } else {
            false
        }
    }

    override fun onBack() = true

    override fun enableNext(): Boolean {
        adapter.dataObservable.subscribe {
            stepper.enableNext(it.map {
                it.first
            }.reduce { first, next ->
                first || next
            }, LanguageFragment::class.java)

            val anyChecked = adapter.anyChecked

            if (!anyChecked) {
                errorCallback.onNoLanguageSelectedError()
            }
        }
        
        return adapter.anyChecked
    }

    override fun enableBack() = true
    
    interface LanguageErrorCallback {
        fun onNoLanguageSelectedError()
    }
}