package org.taskforce.episample.config.settings.display

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.ArrayAdapter
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.settings.user.UserSettingsFragment

class DisplaySettingsViewModel(
        languageService: LanguageService,
        val defaultLanguageAdapter: ArrayAdapter<String>,
        val dateFormatAdapter: ArrayAdapter<String>,
        val timeFormatAdapter: ArrayAdapter<String>,
        val configBuildManager: ConfigBuildManager) : ViewModel(), BaseConfigViewModel {
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

    override val progress: Int
        get() = 6
    override val backEnabled: ObservableField<Boolean> = ObservableField(true)
    override val nextEnabled: ObservableField<Boolean> = ObservableField(true)

    override fun onNextClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager

        fragmentManager
                .beginTransaction()
                .replace(R.id.configFrame, UserSettingsFragment())
                .addToBackStack(UserSettingsFragment::class.qualifiedName)
                .commit()
    }

    override fun onBackClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
        fragmentManager.popBackStack()
    }
}