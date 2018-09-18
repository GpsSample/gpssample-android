package org.taskforce.episample.core.ui.dialogs

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.support.v4.content.ContextCompat
import android.view.View
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.core.language.LanguageService
import javax.inject.Inject

class TextInputDialogViewModel(application: Application,
                               titleResId: Int,
                               hintResId: Int,
                               val submit: (String) -> Unit,
                               val goBack: () -> Unit): AndroidViewModel(application) {
    @Inject
    lateinit var languageService: LanguageService

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }

    val title = languageService.getString(titleResId)
    val okButtonText = languageService.getString(R.string.submit)
    val cancelButtonText = languageService.getString(R.string.go_back)
    val hint = languageService.getString(hintResId)
    val formValue = MutableLiveData<String>()
    val submitEnabled = (Transformations.map(formValue, {
        !it.isNullOrBlank()
    }) as MutableLiveData<Boolean>).apply { value = false }

    val submitTextColor = (Transformations.map(submitEnabled, {
        if (it) {
            ContextCompat.getColor(getApplication(), R.color.colorAccent)
        } else {
            ContextCompat.getColor(getApplication(), R.color.textColorDisabled)
        }
    }) as MutableLiveData<Int>).apply { value = ContextCompat.getColor(getApplication(), R.color.textColorDisabled) }

    fun submit(view: View) {
        submit(formValue.value ?: "")
    }

    fun goBack(view: View) {
        goBack()
    }
}