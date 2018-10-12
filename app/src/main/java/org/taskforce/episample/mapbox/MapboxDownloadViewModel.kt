package org.taskforce.episample.mapbox

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.content.res.Resources
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import org.taskforce.episample.R
import org.taskforce.episample.mapbox.MapboxConfigViewModel.Companion.LAT_OFFSET_TOLERANCE
import org.taskforce.episample.mapbox.MapboxConfigViewModel.Companion.LNG_OFFSET_TOLERANCE
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.config.ResolvedConfig
import org.taskforce.episample.db.config.ResolvedEnumerationArea

class MapboxDownloadViewModel(application: Application, private val resources: Resources) : AndroidViewModel(application) {

    val studyRepository = StudyRepository(application)

    // Mapbox fragment always has a study
    val studyConfig: LiveData<ResolvedConfig> = Transformations.switchMap(studyRepository.getStudy()) {
        it!!.let { study ->
            studyRepository.getResolvedConfig(study.configId)
        }
    }

    val enumerationAreas: LiveData<List<ResolvedEnumerationArea>> = Transformations.map(studyConfig) {
        it.enumerationAreas
    }

    val downloadStatus = MutableLiveData<String>().apply { value = "Awaiting Download" }

    val offlineRegions = MutableLiveData<List<OfflineRegion>>().apply { value = listOf() }

    val isValid: LiveData<Boolean> = Transformations.map(LiveDataPair(studyConfig, offlineRegions)) { (config, offlineRegions) ->
        offlineRegions.forEach {
            val pyramidRegion = it.definition as OfflineTilePyramidRegionDefinition
            val bounds = pyramidRegion.bounds
            val downloadedStyleUrl = pyramidRegion.styleURL
            val minZoom = pyramidRegion.minZoom
            val maxZoom = pyramidRegion.maxZoom

            try {
                if (minZoom != config.mapMinZoom || maxZoom != config.mapMaxZoom) {
                    return@map false
                }
            } catch (e: NumberFormatException) {
                return@map false
            }

            val paddedBounds = bounds
                    .include(LatLng(bounds.latSouth - LAT_OFFSET_TOLERANCE, bounds.lonWest - LNG_OFFSET_TOLERANCE))
                    .include(LatLng(bounds.latNorth + LAT_OFFSET_TOLERANCE, bounds.lonEast + LNG_OFFSET_TOLERANCE))

            if (config.mapboxStyleString != downloadedStyleUrl) {
                return@map false
            }

            var outlyingPointFound = false

            config.enumerationAreas.forEach {
                it.points.forEach {
                    if (!paddedBounds.contains(LatLng(it.lat, it.lng))) {
                        outlyingPointFound = true
                    }
                }
            }

            if (!outlyingPointFound) {
                downloadStatus.postValue(resources.getString(R.string.mapbox_tiles_downloaded))
                return@map true
            }
        }
        return@map false
    }

    val retryButtonVisibility = MutableLiveData<Boolean>().apply { value = false }
    val downloadButtonVisibility: LiveData<Boolean> = Transformations.map(LiveDataPair(isValid, retryButtonVisibility)) { (isValid, retryButtonVisibility) ->
        retryButtonVisibility == false && !isValid
    }
}