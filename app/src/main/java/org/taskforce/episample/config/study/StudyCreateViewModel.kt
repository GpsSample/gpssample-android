package org.taskforce.episample.config.study

import android.app.Application
import android.arch.lifecycle.*
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.config.Config
import org.taskforce.episample.db.config.ResolvedConfig

class StudyCreateViewModel(
        application: Application,
        languageService: LanguageService,
        private val enabledColor: Int,
        private val disabledColor: Int,
        val openStudy: (configId: String, stringId: String) -> Unit,
        configId: String,
        share: Boolean) : AndroidViewModel(application) {

    val configRepository = ConfigRepository(getApplication())
    private val configById = configRepository.getResolvedConfig(configId)
    val name = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    val nameValue: String
        get() = name.value ?: ""
    val passwordValue: String
        get() = password.value ?: ""
    val config: ResolvedConfig?
        get() = configById.value

    init {
        languageService.update = {
            nameHint.value = languageService.getString(R.string.study_name)
            passwordHint.value = languageService.getString(R.string.study_password)
            passwordHelp.value = languageService.getString(R.string.study_password_help)
            transferHelp.value = languageService.getString(R.string.study_transfer_help)
        }
    }

    val nameHint = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_name) }

    val passwordHint = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_password) }

    val passwordHelp = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_password_help) }

    val transferHelp = MutableLiveData<String>().apply { value = languageService.getString(R.string.study_transfer_help) }

    val displayError = MutableLiveData<Boolean>().apply { value = false }

    val isValid: LiveData<Boolean> = Transformations.map(LiveDataPair(name, password)) { (name, password) ->
        return@map name.isNotBlank() && password.isNotBlank()
    }

    val buttonEnabled = (Transformations.map(LiveDataPair(configById, isValid)) { (config, isValid) ->
        isValid
    } as MutableLiveData<Boolean>).apply { value = false }

    val buttonColor = (Transformations.map(isValid) {
        return@map if (it) {
            enabledColor
        } else {
            disabledColor
        }
    } as MutableLiveData<Int>).apply { value = disabledColor }

    var createButtonText = MutableLiveData<String>().apply {
        value = languageService.getString(
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
        configById.value?.let { resolvedConfig ->
            configRepository.insertStudy(resolvedConfig, nameValue, passwordValue, { configId, studyId ->
                openStudy(configId, studyId)
            })
        }
    }
}