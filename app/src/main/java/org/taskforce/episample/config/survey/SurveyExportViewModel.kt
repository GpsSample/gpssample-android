package org.taskforce.episample.config.survey

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.StepperCallback
import org.taskforce.episample.config.fields.CustomFieldDisplayFactory
import org.taskforce.episample.config.language.LanguageService

class SurveyExportViewModel(
        languageService: LanguageService,
        private val configBuildManager: ConfigBuildManager) : ViewModel(), StepperCallback {

    init {
        languageService.update = {
            fieldListTitle.set(languageService.getString(R.string.config_survey_select_list_title))
            adapter.piiWarning = languageService.getString(R.string.config_survey_select_pii_warning)
        }
    }

    val fieldListTitle = ObservableField(languageService.getString(R.string.config_survey_select_list_title))

    val adapter = SurveyExportAdapter(
            CustomFieldDisplayFactory(languageService),
            languageService.getString(R.string.config_survey_select_pii_warning))

    init {
        configBuildManager.customFieldObservable.subscribe(adapter)
    }

    override fun onNext(): Boolean {
        configBuildManager.setFieldExportSettings(adapter.selectedData.map {
            it.key.isExported = it.value
            it.key
        })
        return true
    }

    override fun onBack() = true

    override fun enableNext() = true

    override fun enableBack() = true
}