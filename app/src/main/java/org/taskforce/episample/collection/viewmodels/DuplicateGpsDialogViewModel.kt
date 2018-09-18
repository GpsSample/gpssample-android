package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService

class DuplicateGpsDialogViewModel(private val languageService: LanguageService,
                                  private val enumerationName: String,
                                  private val enumerationSubject: String,
                                  private val latitude: Double,
                                  private val longitude: Double,
                                  private val onCancel: () -> Unit,
                                  private val onDone: () -> Unit): ViewModel() {
    
    val title = MutableLiveData<String>().apply { value = languageService.getString(R.string.collect_duplicate_gps_title) }
    
    val message = MutableLiveData<String>().apply { value = languageService.getString(R.string.collect_duplicate_gps_dialog, 
            enumerationSubject, 
            enumerationName,
            "%.5f".format(latitude),
            "%.5f".format(longitude)) }
    
    val confirm = MutableLiveData<String>().apply { value = languageService.getString(R.string.use_last_recorded) }
    
    val cancel = MutableLiveData<String>().apply { value = languageService.getString(R.string.cancel) }
    
    fun goBack() {
        onCancel()
    }
    
    fun confirmUse() {
        onDone()
        goBack()
    }
}