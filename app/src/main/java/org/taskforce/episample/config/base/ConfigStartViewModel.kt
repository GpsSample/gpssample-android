package org.taskforce.episample.config.base

import android.app.Application
import android.arch.lifecycle.*
import android.databinding.ObservableField
import android.view.View
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.config.Config


class ConfigStartViewModel(application: Application,
                           val createNewConfiguration: () -> Unit,
                           val showAllConfigurations: () -> Unit,
                           val signIn: (Config, String) -> Unit,
                           transferManager: TransferManager) : AndroidViewModel(application) {

    val configRepository = ConfigRepository(getApplication())
    private var studyRepository = StudyRepository(getApplication())
    val availableConfigs = configRepository.getAvailableConfigs()
    val studyConfigs = studyRepository.getAllConfigs()

    val study = studyRepository.getStudy()

    //    Header Text
    // TODO move formatting string here if we can get languageManager back without a leak
    val configCount = MutableLiveData<String>()

    val viewConfigurationsVisibility = MutableLiveData<Boolean>()
    val studyVisibility: LiveData<Boolean> = Transformations.map(study, {
        return@map it != null
    })

    val configsObserver = Observer<List<Config>> {
        viewConfigurationsVisibility.value = it?.size ?: 0 > 0
        configCount.value = it?.size?.toString() ?: 0.toString()
    }

    init {
        viewConfigurationsVisibility.value = false
        configCount.value = 0.toString()
        availableConfigs.observeForever(configsObserver)
    }

    override fun onCleared() {
        super.onCleared()
        availableConfigs.removeObserver(configsObserver)
        configRepository.cleanUp()
        studyRepository.cleanUp()
    }

    val currentStudyDate: LiveData<String> = Transformations.map(study, {
        return@map it?.dateCreatedDisplay ?: ""
    })

    val currentStudyName: LiveData<String> = Transformations.map(study, {
        return@map it?.name ?: ""
    })

    val enableSignIn = LiveDataPair(study, studyConfigs)
    val studySignInEnabled = Transformations.map(enableSignIn) {
        it.first != null && it.second.isNotEmpty()
    }

    val uploadUrl = ObservableField("")

    init {
        transferManager.transferStateObservable.subscribe {
            uploadUrl.set("http://${it.ipAddress}")
        }
    }

    fun signIn(view: View) {
        studyConfigs.value?.let { configs ->
            study.value?.let { study ->
                val studyConfig = configs.first { it.id == study.configId }
                signIn.invoke(studyConfig, study.id)
            }
        }
    }

    companion object {
        const val HELP_TARGET = "#configSelect"
    }

}