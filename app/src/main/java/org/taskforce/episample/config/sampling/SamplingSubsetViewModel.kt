package org.taskforce.episample.config.sampling

import android.databinding.BaseObservable
import org.taskforce.episample.config.language.LanguageService

class SamplingSubsetViewModel(languageService: LanguageService) : BaseObservable() {

    init {
        languageService.update = {

        }
    }

}