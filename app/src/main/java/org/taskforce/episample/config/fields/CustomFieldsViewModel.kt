package org.taskforce.episample.config.fields

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.base.StepperCallback
import org.taskforce.episample.config.language.LanguageService

class CustomFieldsViewModel(
        val languageService: LanguageService,
        private val stepper: Stepper,
        val createNewField: () -> Unit,
        private val configBuildManager: ConfigBuildManager) :
        ViewModel(), StepperCallback {

    init {
        languageService.update = {
            hint.set(it.getString(R.string.config_fields_hint))
            fieldHeader.set(it.getString(R.string.config_fields_list_title))
            explanation2.set(it.getString(R.string.config_fields_explanation_2))
            createNewFieldText.set(it.getString(R.string.config_fields_add))
        }
    }

    val isFormValid: Boolean
        get() {
            return !subject.get().isNullOrBlank()
        }

    val subject = ObservableField(languageService.getString(R.string.default_subject))

    val hint = ObservableField(languageService.getString(R.string.config_fields_hint))

    val error = object: ObservableField<String>(subject) {
        override fun get(): String? {
            if (isFormValid) { return "" }

            return languageService.getString(R.string.config_fields_subject_error)
        }
    }

    val fieldHeader = ObservableField(languageService.getString(R.string.config_fields_list_title))

    val explanation2 = object: ObservableField<String>(subject) {
        override fun get(): String? = languageService.getString(R.string.config_fields_explanation_2, subject.get()!!)

        override fun set(value: String?) {
            stepper.enableNext(isFormValid, CustomFieldsFragment::class.java)
        }
    }

    val createNewFieldText = ObservableField(languageService.getString(R.string.config_fields_add))

    val customFieldAdapter = CustomFieldAdapter(
            CustomFieldDisplayFactory(languageService),
            languageService.getString(R.string.config_fields_description_automatic),
            languageService.getString(R.string.config_fields_description_primary),
            languageService.getString(R.string.config_fields_description_contains_pii))

    init {
        (configBuildManager.defaultCustomFields(languageService) - configBuildManager.config.customFields).forEach {
            configBuildManager.addCustomField(it)
        }
        configBuildManager.customFieldObservable.subscribe(customFieldAdapter)
    }

    override fun onNext(): Boolean {
        configBuildManager.setEnumerationSubject(subject.get()!!)
        return true
    }

    override fun onBack() = true

    override fun enableNext() = true

    override fun enableBack() = true
}

interface CustomFieldDefaultProvider {
    fun defaultCustomFields(languageService: LanguageService): List<CustomField>
}