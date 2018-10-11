package org.taskforce.episample.config.mapbox

import android.arch.lifecycle.ViewModel
import android.content.res.Resources
import android.databinding.ObservableField
import android.support.v4.app.FragmentActivity
import android.view.View
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import org.taskforce.episample.R
import org.taskforce.episample.config.base.BaseConfigViewModel
import org.taskforce.episample.config.base.ConfigBuildManager
import org.taskforce.episample.config.fields.CustomFieldsFragment
import org.taskforce.episample.core.models.MapboxStyleUrl
import org.taskforce.episample.core.models.MapboxStyleUrl.Companion.DEFAULT_MAX_ZOOM
import org.taskforce.episample.core.models.MapboxStyleUrl.Companion.DEFAULT_MIN_ZOOM

class MapboxConfigViewModel(private val latLngBounds: LatLngBounds, private val resources: Resources, private val configBuildManager: ConfigBuildManager) : ViewModel(), BaseConfigViewModel {

    val offlineRegions = ObservableField<List<OfflineRegion>>(listOf())
    val styleUrl = ObservableField<String>(MapboxStyleUrl.DEFAULT_MAPBOX_STYLE)

    val maxZoomString = ObservableField(DEFAULT_MAX_ZOOM.toString())
    val minZoomString = ObservableField(DEFAULT_MIN_ZOOM.toString())

    val isValid = object : ObservableField<Boolean>(styleUrl, offlineRegions, maxZoomString, minZoomString) {
        override fun get(): Boolean? {
            offlineRegions.get()?.forEach {
                val pyramidRegion = it.definition as OfflineTilePyramidRegionDefinition
                val bounds = pyramidRegion.bounds
                val downloadedStyleUrl = pyramidRegion.styleURL
                val minZoom = pyramidRegion.minZoom
                val maxZoom = pyramidRegion.maxZoom

                try {
                    if (minZoom != minZoomString.get()?.toDouble() ||
                            maxZoom != maxZoomString.get()?.toDouble()) {
                        return false
                    }
                } catch (e: NumberFormatException) {
                    return false
                }

                val paddedBounds = bounds
                        .include(LatLng(bounds.latSouth - LAT_OFFSET_TOLERANCE, bounds.lonWest - LNG_OFFSET_TOLERANCE))
                        .include(LatLng(bounds.latNorth + LAT_OFFSET_TOLERANCE, bounds.lonEast + LNG_OFFSET_TOLERANCE))

                styleUrl.get().let {
                    if (it != downloadedStyleUrl) {
                        return false
                    }
                }

                var outlyingPointFound = false

                configBuildManager.config.enumerationAreas.forEach {
                    it.points.forEach {

                        if (!paddedBounds.contains(LatLng(it.first, it.second))) {
                            outlyingPointFound = true
                        }
                    }
                }

                if (!outlyingPointFound) {
                    return true
                }
            }
            return false
        }
    }

    val southWestLatLngString: String
        get() = resources.getString(R.string.southwest_lat_lng_var, latLngBounds.southWest.latitude, latLngBounds.southWest.longitude)

    val northEastLatLngString: String
        get() = resources.getString(R.string.northeast_lat_lng_var, latLngBounds.northEast.latitude, latLngBounds.northEast.longitude)

    override val backEnabled = ObservableField<Boolean>(true)
    override val nextEnabled: ObservableField<Boolean> = object : ObservableField<Boolean>(isValid) {
        override fun get(): Boolean? {
            return isValid.get() ?: false
        }
    }

    val downloadEnabled = object : ObservableField<Boolean>(styleUrl, isValid, maxZoomString, minZoomString) {
        override fun get(): Boolean? {
            return maxZoomString.get()?.let { maxString ->
                minZoomString.get()?.let { minString ->
                    try {
                        maxString.toDouble() <= MAX_ZOOM &&
                                minString.toDouble() >= MIN_ZOOM &&
                                styleUrl.get()?.isBlank() == false && isValid.get() == false
                    } catch(e: NumberFormatException) {
                        false
                    }
                }
            } ?: false
        }
    }

    val testStyleEnabled = object : ObservableField<Boolean>() {
        override fun get(): Boolean? {
            return isValid.get() == false
        }
    }

    override fun onNextClicked(view: View) {
        styleUrl.get()?.let {
            configBuildManager.setMapboxStyle(MapboxStyleUrl(it))
            val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.configFrame, CustomFieldsFragment())
                    .addToBackStack(CustomFieldsFragment::class.qualifiedName)
                    .commit()
        }
    }

    override fun onBackClicked(view: View) {
        val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
        fragmentManager.popBackStack()
    }

    companion object {
        const val MAX_ZOOM = 16.0
        const val MIN_ZOOM = 1.0
        val LAT_OFFSET_TOLERANCE = Math.pow(1.0, -14.0)
        val LNG_OFFSET_TOLERANCE = Math.pow(1.0, -14.0)
    }
}