package org.taskforce.episample.config.geography

import android.arch.lifecycle.ViewModel
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService

class OutsideAreaDialogViewModel(languageService: LanguageService,
                                 val continueSave: () -> Unit,
                                 val goBack: () -> Unit): ViewModel() {
    
    val title = languageService.getString(R.string.collect_outside_area_title)
    
    val message = languageService.getString(R.string.collect_outside_area_message)
    
    val saveButtonText = languageService.getString(R.string.save).toUpperCase()
    
    val cancelButtonText = languageService.getString(R.string.cancel)
    
    fun save() {
        continueSave()
        goBack()
    }
    
    fun cancel() {
        goBack()
    }
}