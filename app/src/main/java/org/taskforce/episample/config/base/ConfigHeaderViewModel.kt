package org.taskforce.episample.config.base

import android.databinding.BaseObservable
import android.databinding.Bindable
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.utils.bindDelegate

class ConfigHeaderViewModel(
        private val languageService: LanguageService,
        private val titleRes: Int,
        private val explanationRes: Int,
        vararg explanationArguments: String) : BaseObservable() {

    init {
        languageService.update = {
            title = languageService.getString(titleRes)
            explanation = languageService.getString(explanationRes, *arguments)
        }
    }

    private val arguments = explanationArguments

    @get:Bindable
    var title by bindDelegate(languageService.getString(titleRes))

    @get:Bindable
    var explanation by bindDelegate(languageService.getString(explanationRes, *arguments))

}