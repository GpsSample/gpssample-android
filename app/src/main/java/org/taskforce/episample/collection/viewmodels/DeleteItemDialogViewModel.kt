package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.ViewModel
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService

class DeleteItemDialogViewModel(languageService: LanguageService,
                                val delete: () -> Unit,
                                val goBack: () -> Unit,
                                val subject: String): ViewModel() {

    val title = languageService.getString(R.string.delete_item_title)

    val message = languageService.getString(R.string.delete_item_message, subject)
    
    val deleteButtonText = languageService.getString(R.string.delete)

    val cancelButtonText = languageService.getString(R.string.cancel)
    
    fun confirmDelete() {
        delete()
        goBack()
    }

    fun cancel() {
        goBack()
    }
}