package org.taskforce.episample.config.geography

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.language.LanguageService

class GeographyViewModelFactory(private val languageService: LanguageService,
                                private val stepper: Stepper,
                                private val adapter: EnumerationAreaAdapter) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GeographyViewModel(languageService,
                stepper,
                adapter) as T
    }
}