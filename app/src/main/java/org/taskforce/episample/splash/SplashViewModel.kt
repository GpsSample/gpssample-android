package org.taskforce.episample.splash

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Transformations
import org.taskforce.episample.db.ConfigRepository

class SplashViewModel(
        application: Application): AndroidViewModel(application) {

    val configRepository = ConfigRepository(getApplication())
    val studyConfig = Transformations.map(configRepository.getAllConfigs(), {
        return@map it.filter { c -> c.studyId != null }
                .firstOrNull()
    })
}