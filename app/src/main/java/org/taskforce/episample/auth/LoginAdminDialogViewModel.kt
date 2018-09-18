package org.taskforce.episample.auth

import android.app.Application
import android.arch.lifecycle.*
import android.support.v4.content.ContextCompat
import org.taskforce.episample.R
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.db.ConfigRepository

class LoginAdminDialogViewModel(
        application: Application,
        configId: String,
        val title: String,
        val hint: String,
        val cancel: String,
        val done: String,
        private val errorSource: String,
        val onCancel: () -> Unit,
        val onDone: () -> Unit) : AndroidViewModel(application) {

    val configRepository = ConfigRepository(application)
    val password: LiveData<String> = Transformations.map(configRepository.getResolvedConfig(configId)) {
        it.adminSettings.password
    }

    val errorEnabled = true
    val error = MutableLiveData<String?>()
    val input = MutableLiveData<String>()

    val doneEnabled: LiveData<Boolean> = (Transformations.map(LiveDataPair(password, input)) { (_, formValue) ->
        formValue.isNotBlank()
    } as MutableLiveData).apply { value = false }

    val doneTextColor: LiveData<Int> = (Transformations.map(doneEnabled) {
        if (it) {
            ContextCompat.getColor(getApplication(), R.color.colorAccent)
        } else {
            ContextCompat.getColor(getApplication(), R.color.textColorDisabled)
        }
    } as MutableLiveData).apply { value = ContextCompat.getColor(getApplication(), R.color.textColorDisabled) }

    private val inputObserver: Observer<String> = Observer {
        error.postValue(null)
    }

    init {
        input.observeForever(inputObserver)
    }

    override fun onCleared() {
        super.onCleared()
        input.removeObserver(inputObserver)
    }

    fun cancel() {
        onCancel()
    }

    fun done() {
        password.value?.let { password ->
            input.value?.let { input ->
                if (password == input) {
                    onDone()
                    cancel()
                } else {
                    error.postValue(errorSource)
                }
            }
        }
    }
}