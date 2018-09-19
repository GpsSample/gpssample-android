package org.taskforce.episample.config.base

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import io.reactivex.Observable
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.core.language.LiveLanguageService
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.config.Config


class ConfigAllViewModel(
        application: Application,
        languageService: LanguageService,
        transferManager: TransferManager,
        createStudy: (org.taskforce.episample.db.config.Config) -> Unit,
        val openConfigEdit: () -> Unit,
        showError: (String) -> Unit) : AndroidViewModel(application) {

    lateinit var back: () -> Unit

    val configRepository = ConfigRepository(getApplication())
    val studyRepository = StudyRepository(getApplication())
    val availableConfigs = configRepository.getAvailableConfigs()
    val study = studyRepository.getStudy()

    val networkAddress = MutableLiveData<String>()

    val title= languageService.getString(R.string.configs)
    val transferExplanation = languageService.getString(R.string.config_all_transfer_explanation)
    val configCountText = Transformations.map(availableConfigs) {
        languageService.getString(R.string.config_count, it.size.toString())
    }

    val menuObservable =
            Observable.just(listOf(
                    ConfigItemMenuViewModel(
                            R.drawable.icon_attach_darkgray_24,
                            languageService.getString(R.string.config_all_menu_create), {
                        createStudy(it)
                    }),
                    ConfigItemMenuViewModel(
                            R.drawable.icon_copy_darkgray_24,
                            languageService.getString(R.string.config_all_menu_duplicate), {
                        try {
                            configRepository.duplicateConfig(it, {
                                // no op new config shows up in list when database changes
                            })
                        } catch (cme: ConfigManagerException) {
                            showError(cme.localizedMessage)
                        }
                    }),
                    ConfigItemMenuViewModel(
                            R.drawable.icon_share_darkgray_24,
                            languageService.getString(R.string.config_all_menu_share), {
                        //TODO: open sync workflow
                    }),
                    ConfigItemMenuViewModel(
                            R.drawable.icon_delete_darkgray_24,
                            languageService.getString(R.string.config_all_menu_delete), {
                        configRepository.deleteConfig(it, {
                            // no op deleted config is removed from list when database changes
                        })
                    })
            ))

    val configObserver: Observer<List<Config>> = Observer {
        if (it?.isEmpty() ?: true) {
            back()
        }
    }

    init {
        transferManager.transferStateObservable.subscribe {
            networkAddress.value = "http://${it.ipAddress}"
        }
        availableConfigs.observeForever(configObserver)
    }

    override fun onCleared() {
        super.onCleared()
        availableConfigs.removeObserver(configObserver)
    }
}