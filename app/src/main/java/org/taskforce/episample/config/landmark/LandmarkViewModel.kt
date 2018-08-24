package org.taskforce.episample.config.landmark

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import io.reactivex.Observable
import org.taskforce.episample.R
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.config.base.StepperCallback
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.fileImport.models.LandmarkType

class LandmarkViewModel(
        languageService: LanguageService,
        private val stepper: Stepper,
        val addLandmark: () -> Unit,
        val addLandmarksToConfig: () -> Unit,
        val removeLandmarks: (landmarkType: LandmarkType) -> Unit,
        val editLandmarkType: (landmarkType: LandmarkType) -> Unit,
        landmarkObservable: Observable<List<LandmarkType>>) : ViewModel(), StepperCallback, LandmarkAdapter.LandmarkUpdateCallback {

    val landmarkHeader = ObservableField(languageService.getString(R.string.config_landmarks_types))

    val addLandmarkText = ObservableField(languageService.getString(R.string.config_landmarks_types_add))

    val skip = ObservableField(languageService.getString(R.string.config_landmarks_skip))

    val skipVisibility = ObservableField(true)

    val landmarkError = ObservableField(R.string.config_list_empty)

    var landmarkErrorVisibility = ObservableField(true)

    val landmarkAdapter = LandmarkAdapter(this)

    init {
        landmarkObservable.subscribe(landmarkAdapter)
        landmarkObservable.subscribe {
            landmarkErrorVisibility.set(it.isEmpty())
            skipVisibility.set(it.isEmpty())
        }

        languageService.update = {
            landmarkHeader.set(languageService.getString(R.string.config_landmarks_types))
            addLandmarkText.set(languageService.getString(R.string.config_landmarks_types_add))
            skip.set(languageService.getString(R.string.config_landmarks_skip))
        }
    }

    override fun editLandmark(landmarkType: LandmarkType) {
        editLandmarkType(landmarkType)
    }

    override fun removeLandmark(landmarkType: LandmarkType) {
        removeLandmarks(landmarkType)
    }

    fun skip() {
        stepper.next()
    }

    override fun onNext(): Boolean {
        addLandmarksToConfig()
        return true
    }

    override fun onBack(): Boolean {
        return true
    }

    override fun enableNext() = true

    override fun enableBack() = true
}