package org.taskforce.episample.toolbar.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.taskforce.episample.core.language.LanguageDescription
import org.taskforce.episample.core.language.LanguageService
import org.taskforce.episample.navigation.ui.LanguageAdapter

class AppToolbarViewModel(titleResId: Int, languageService: LanguageService) : ViewModel() {
    val languageSelectVisibility = MutableLiveData<Boolean>().apply { value = false }
    var title = languageService.getString(titleResId)

    val languageAdapter = LanguageAdapter(
            LanguageDescription(languageService.currentLanguage.value!!.id,
                    languageService.currentLanguage.value!!.name),
            languageService.getAvailableLanguages(),
            {
                languageService.updateCurrentLanguage(it)
                languageSelectVisibility.postValue(false)
            })
}