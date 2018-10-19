package org.taskforce.episample.collection.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import com.mapbox.mapboxsdk.geometry.LatLng
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.core.interfaces.LocationService
import javax.inject.Inject

class CollectViewModel(application: Application,
                       languageService: LanguageService,
                       val addPoint: (Boolean) -> Unit,
                       val back: () -> Unit) : AndroidViewModel(application) {

    @Inject
    lateinit var collectManager: CollectManager

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var locationService: LocationService

    init {
        (application as EpiApplication).collectComponent?.inject(this)

        locationService.collectManager = collectManager
    }

    val enumerationSubject: EnumerationSubject = config.enumerationSubject

    val collectItems = collectManager.getCollectItems()

    private val collectDescriptionLiveData = LiveDataPair(collectManager.getEnumerations(),
            collectManager.getLandmarks())

    init {
        languageService.update = {
            descriptionText.value = "${subjectCount.value} ${config.enumerationSubject.plural}, " +
                    "${landmarkCount.value} ${languageService.getString(R.string.config_landmarks_title)}"
        }
    }

    private val subjectCount: LiveData<Int> = Transformations.map(collectManager.getEnumerations()) { it.size }

    private val landmarkCount: LiveData<Int> = Transformations.map(collectManager.getLandmarks()) { it.size }

    val descriptionText = Transformations.map(collectDescriptionLiveData) {
        val enumerations = it.first
        val landmarks = it.second
        val enumerationSubject = config.enumerationSubject

        "${enumerations.size} ${enumerationSubject.plural.capitalize()}, " +
                "${landmarks.size} ${languageService.getString(R.string.config_landmarks_title)}"
    } as MutableLiveData<String>

    val householdButtonText = ObservableField<String>("+ ${enumerationSubject.singular}")

    val landmarkButtonText = MutableLiveData<String>().apply {
        value = languageService.getString(R.string.collect_button_landmark)
    }

    var lastKnownLocation: LatLng? = null

    companion object {
        val breadcrumbAccuracy: Float = 5F
        val zoomLevel = 18.0
    }
}