package org.taskforce.episample.config.study

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.config.Config
import org.taskforce.episample.db.config.ResolvedConfig

class StudyCreateViewModel(
        application: Application,
        languageService: LanguageService,
        val enabledColor: Int,
        val disabledColor: Int,
        val openStudy: (configId: String, stringId: String) -> Unit,
        configId: String,
        share: Boolean) : AndroidViewModel(application) {

    val configRepository = ConfigRepository(getApplication())
    val configById = configRepository.getResolvedConfig(configId)
    val name = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    val textListener: Observer<String> = Observer {
        mapStateToView()
    }

    val configObserver: Observer<ResolvedConfig> = Observer {
        resolvedConfig = it
    }

    var resolvedConfig: ResolvedConfig? = null

    init {
        languageService.update = {
            nameHint.value = languageService.getString(R.string.study_name)
            passwordHint.value = languageService.getString(R.string.study_password)
            passwordHelp.value = languageService.getString(R.string.study_password_help)
            transferHelp.value = languageService.getString(R.string.study_transfer_help)
        }

        name.observeForever(textListener)
        password.observeForever(textListener)
        configById.observeForever(configObserver)
        configById
    }

    override fun onCleared() {
        super.onCleared()

        name.removeObserver(textListener)
        password.removeObserver(textListener)
        configById.removeObserver(configObserver)
    }

    val nameHint = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_name) }

    val passwordHint = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_password) }

    val passwordHelp = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_password_help) }

    val transferHelp = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_transfer_help) }

    val displayError = MutableLiveData<Boolean>().apply { value = false }

    val buttonEnabled = MutableLiveData<Boolean>().apply { value = false }

    val buttonColor = MutableLiveData<Int>().apply { value = disabledColor }

    var createButtonText = MutableLiveData<String>().apply { value = languageService.getString(
            if (share) {
                R.string.study_create_share
            } else {
                R.string.study_create
            })
    }

    var errorButtonText = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_create_error_button) }

    var errorText = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_create_error) }

    fun createStudy() {
        displayError.value = false
        configRepository.insertStudy(resolvedConfig!!, name.value!!, password.value!!, { configId, studyId ->
            openStudy(configId, studyId)
        })
    }

    val isValid: Boolean
        get() = name.value?.isNotBlank() == true && password.value?.isNotBlank() == true

    private fun mapStateToView() {
        buttonEnabled.value = isValid
        buttonColor.value = if (isValid) {
            enabledColor
        } else {
            disabledColor
        }
    }
}