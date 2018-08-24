package org.taskforce.episample.config.landmark

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import io.reactivex.Observable
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.fileImport.models.LandmarkType

class LandmarkViewModelFactory(private val languageService: LanguageService,
                               private val stepper: Stepper,
                               private val addLandmark: () -> Unit,
                               private val addLandmarksToConfig: () -> Unit,
                               private val removeLandmarks: (landmarkType: LandmarkType) -> Unit,
                               private val editLandmark: (landmarkType: LandmarkType) -> Unit,
                               private val landmarkObservable: Observable<List<LandmarkType>>): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LandmarkViewModel(languageService,
                stepper,
                addLandmark,
                addLandmarksToConfig,
                removeLandmarks,
                editLandmark,
                landmarkObservable) as T
    }
}