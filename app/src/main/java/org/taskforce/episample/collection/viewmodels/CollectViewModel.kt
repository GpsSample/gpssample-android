package org.taskforce.episample.collection.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.Single
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.LiveDataTriple
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.mock.MockBreadcrumb
import javax.inject.Inject

class CollectViewModel(application: Application,
        languageService: LanguageService,
        googleMapSingle: Single<GoogleMap>,
        lastKnownLocationObservable: Observable<Pair<LatLng, Float>>,
        val addPoint: (Boolean) -> Unit,
        val back: () -> Unit) : AndroidViewModel(application) {

    @Inject
    lateinit var collectManager: CollectManager

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }

    val collectItems = collectManager.getCollectItems()
    
    val gpsBreadcrumbs = collectManager.getBreadcrumbs()
    
    val enumerationSubject = collectManager.getEnumerationSubject()
    
    val userSettings = collectManager.getUserSettings()
    
    val displaySettings = collectManager.getDisplaySettings()
    
    val userDisplaySettingsSubjectTriple = LiveDataTriple(enumerationSubject, userSettings, displaySettings)
    
    private val collectDescriptionLiveData = LiveDataTriple(collectManager.getEnumerations(), 
            collectManager.getLandmarks(),
            collectManager.getEnumerationSubject())

    init {
        languageService.update = {
            descriptionText.value = "${subjectCount.value} ${collectManager.getEnumerationSubject().value?.plural}, " +
                    "${landmarkCount.value} ${languageService.getString(R.string.config_landmarks_title)}"
        }

        lastKnownLocationObservable.subscribe {
            val location = it.first
            val accuracy = it.second
            
            if (lastKnownLocation == null) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
                lastKnownLocation = location
            }
            
            val distanceResult = FloatArray(1)
            Location.distanceBetween(lastKnownLocation!!.latitude, 
                                        lastKnownLocation!!.longitude,
                                        location.latitude,
                                        location.longitude, 
                                        distanceResult)
            
            val distance = distanceResult[0]
            if (distance >= breadcrumbAccuracy) {
                collectManager.addBreadcrumb(MockBreadcrumb(accuracy.toDouble(), location))
            }
            
            lastKnownLocation = location
        }
        googleMapSingle.subscribe(
                {
                    googleMap = it
                    lastKnownLocation?.let { location ->
                        it.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f))
                    }
                },
                {
                    //TODO: Display unable to load Google Map.
                }
        )
    }

    private val subjectCount: LiveData<Int> = Transformations.map(collectManager.getEnumerations()) { it.size }

    private val landmarkCount: LiveData<Int> = Transformations.map(collectManager.getLandmarks()) { it.size }

    val descriptionText = Transformations.map(collectDescriptionLiveData) {
        val enumerations = it.first
        val landmarks = it.second
        val enumerationSubject = it.third
        
        "${enumerations?.size ?: 0} ${enumerationSubject?.plural?.capitalize()}, " +
                "${landmarks?.size ?: 0} ${languageService.getString(R.string.config_landmarks_title)}"
    } as MutableLiveData<String>
    
    val householdButtonText: LiveData<String> = Transformations.map(collectManager.getEnumerationSubject()) { "+ ${it.singular}" }

    val landmarkButtonText = MutableLiveData<String>().apply {
        value = languageService.getString(R.string.collect_button_landmark)
    }

    private var googleMap: GoogleMap? = null

    private var lastKnownLocation: LatLng? = null
    
    companion object {
        val breadcrumbAccuracy: Float = 5F
        val zoomLevel = 18.0f
    }
}