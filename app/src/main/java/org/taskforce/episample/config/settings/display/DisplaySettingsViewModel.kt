package org.taskforce.episample.config.settings.display

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.widget.ArrayAdapter
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.base.StepperCallback
import org.taskforce.episample.config.language.LanguageService

class DisplaySettingsViewModel(
        languageService: LanguageService,
        val defaultLanguageAdapter: ArrayAdapter<String>,
        val dateFormatAdapter: ArrayAdapter<String>,
        val timeFormatAdapter: ArrayAdapter<String>,
        val configBuildManager: ConfigBuildManager) : ViewModel(), StepperCallback {

    init {
        languageService.update = {
            languageTitle .set(languageService.getString(R.string.config_display_default_language))
            dateFormatTitle.set(languageService.getString(R.string.config_display_date_format))
            timeFormatTitle.set(languageService.getString(R.string.config_display_time_format))
        }
    }

    var languageTitle = ObservableField(languageService.getString(R.string.config_display_default_language))

    var dateFormatTitle = ObservableField(languageService.getString(R.string.config_display_date_format))

    var timeFormatTitle = ObservableField(languageService.getString(R.string.config_display_time_format))

    override fun onNext() = true

    override fun onBack() = true

    override fun enableNext() = true

    override fun enableBack() = true
}