package org.taskforce.episample.splash

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Transformations
import org.taskforce.episample.db.StudyRepository

class SplashViewModel(
        application: Application): AndroidViewModel(application) {

    val studyRepository = StudyRepository(getApplication())
    val study = Transformations.map(studyRepository.getAllStudies(), {
        return@map it.firstOrNull()
    })
}