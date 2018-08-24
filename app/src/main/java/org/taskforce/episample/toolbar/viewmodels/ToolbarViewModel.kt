package org.taskforce.episample.toolbar.viewmodels

import android.databinding.BaseObservable
import android.databinding.Bindable
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.help.HelpManager
import org.taskforce.episample.toolbar.ui.LanguageAdapter
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.bindDelegate

class ToolbarViewModel(
        languageService: LanguageService,
        languageManager: LanguageManager,
        var HELP_TARGET: String,
        var backAction: (() -> Unit)? = null) : BaseObservable() {

    init {
        languageService.update = {
            title = languageService.getString(R.string.app_name)
        }
    }

    @get:Bindable
    var backVisibility by bindDelegate(backAction != null)

    @get:Bindable
    var titleVisibility by bindDelegate(true)

    @get:Bindable
    var languageSelectVisibility by bindDelegate(false)

    @get:Bindable
    var title by bindDelegate(languageManager.getString(R.string.app_name))

    val languageAdapter = LanguageAdapter({
        languageManager.selectLanguage(it)
    })

    init {
        languageManager.currentLanguagesObservable.subscribe(languageAdapter)
        languageManager.languageEventObservable.subscribe {
            languageSelectVisibility = false
        }
    }

    fun onDestroy() {
        languageAdapter.disposable.dispose()
    }

    fun openHelp() {
        HelpManager().openHelp(HELP_TARGET)
    }

    fun showLanguages() {
        languageSelectVisibility = true
    }
}