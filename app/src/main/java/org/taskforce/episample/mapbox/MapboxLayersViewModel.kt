package org.taskforce.episample.mapbox

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.config.ResolvedConfig

class MapboxLayersViewModel(application: Application): AndroidViewModel(application) {
    private val studyRepository = StudyRepository(application)

    // Mapbox fragment always has a study
    val studyConfig: LiveData<ResolvedConfig> = Transformations.switchMap(studyRepository.getStudy()) {
        it!!.let { study ->
            studyRepository.getResolvedConfig(study.configId)
        }
    }

}