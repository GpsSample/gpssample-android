package org.taskforce.episample.config.base

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.view.View
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.StudyRepository

class ConfigSuccessViewModel(
        application: Application,
        private val languageService: LanguageService,
        val backToMain: () -> Unit,
        val createNewStudy: (String) -> Unit,
        val configManager: ConfigManager,
        val config: Config,
        val defaultColor: Int,
        val successColor: Int,
        val failureColor: Int) : AndroidViewModel(application) {

    var insertedConfigId: String? = null

    val configRepository = ConfigRepository(getApplication())
    val studyRepository = StudyRepository(getApplication())
    val study = studyRepository.getStudy()

    val shareClickable = MutableLiveData<Boolean>().apply { value = false }
    val createStudyClickable = MutableLiveData<Boolean>().apply { value = false }

    val cardTextColor = MutableLiveData<Int>().apply { value = defaultColor }
    val buttonBackgroundColor = MutableLiveData<Int>().apply { value = defaultColor }
    val configSuccessBack = MutableLiveData<String>().apply { value = languageService.getString(R.string.config_success_back) }
    val showError = MutableLiveData<Boolean>().apply { value = true }



    val savedSuccessfully = MutableLiveData<Boolean?>().apply { value = true }
    val saveSuccessObserver: Observer<Boolean?> = Observer {

        shareClickable.value = it ?: false
        createStudyClickable.value = it ?: false
        showError.value = it == false

        if (it == true) {
            cardTextColor.value = successColor
            buttonBackgroundColor.value = successColor
        } else {
            cardTextColor.value = failureColor
            buttonBackgroundColor.value = failureColor
            configSuccessBack.value = languageService.getString(R.string.config_success_retry)
        }
    }

    init {
        languageService.update = {
            configSuccessBack.value = if (savedSuccessfully.value != false) {
                languageService.getString(R.string.config_success_back)
            } else {
                languageService.getString(R.string.config_success_retry)
            }
        }

        savedSuccessfully.value = true
        configRepository.insertConfigFromBuildManager(config, {
            // TODO handle DB errors
            savedSuccessfully.postValue(true)
            insertedConfigId = it
        })

        savedSuccessfully.observeForever(saveSuccessObserver)
    }

    override fun onCleared() {
        super.onCleared()
        savedSuccessfully.removeObserver(saveSuccessObserver)
        configRepository.cleanUp()
        studyRepository.cleanUp()
    }

    fun buttonWrapper() {
        if (savedSuccessfully.value == true) {
            backToMain()
        }
        else if (savedSuccessfully.value  == false) {
            savedSuccessfully.value = null

            configManager.addConfig(config).subscribe({
                savedSuccessfully.value = true
            }, {
                savedSuccessfully.value = false
            })
        }
    }


    val errorMessageText = MutableLiveData<String>().apply {
        value = languageService.getString(R.string.config_success_error)
    }

    val share = MutableLiveData<String>().apply { value = languageService.getString(R.string.share) }

    val createStudy = MutableLiveData<String>().apply { value = languageService.getString(R.string.config_success_create_study) }

    val title = MutableLiveData<String>().apply { value = languageService.getString(R.string.config) }

    val date = MutableLiveData<String>().apply { value = languageService.getString(R.string.config_success_created, config.displayDateCreated) }

    fun onCreateStudy(view: View) {
        createNewStudy(insertedConfigId!!)
    }
}