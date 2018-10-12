package org.taskforce.episample.collection.managers

import android.content.Context
import android.content.SharedPreferences
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.Polyline
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.Property.NONE
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.Breadcrumb
import org.taskforce.episample.core.interfaces.CollectItem
import org.taskforce.episample.mapbox.MapboxLayerSetting
import org.taskforce.episample.utils.toMapboxLatLng
import kotlin.math.cos
import kotlin.math.sin

class MapboxItemMarkerManager(
        private val context: Context,
        private val sharedLayerPreferences: SharedPreferences,
        val mapboxMap: MapboxMap) {

    private var showBreadcrumbs = true

    private val markers = mutableListOf<Pair<CollectItem, Marker>>()
    private var currentLocationMarker: Marker? = null
    private var currentLocationPolyline: Polyline? = null
    private val userIcon = MapboxIconFactory.getMapboxIcon(context, R.drawable.map_icon_user)

    private var currentBreacrumbPolylineOptions = listOf<PolylineOptions>()
    private var currentBreacrumbPolylines = listOf<Polyline>()

    fun applyLayerSettings() {
        layerSettings.forEach { layerSetting ->
            mapboxMap.layers.filter { layer ->
                layer.id == layerSetting.name
            }.firstOrNull()
                    ?.let {
                        val visibilityBoolean = sharedLayerPreferences.getBoolean(it.id, true)
                        val layerVisibility = if (visibilityBoolean) {
                            VISIBLE
                        } else {
                            NONE
                        }
                        it.setProperties(visibility(layerVisibility))
                    }
        }
    }

    private val layerSettings: List<MapboxLayerSetting>
        get() = mapboxMap.layers.map {
            val visibility = if (sharedLayerPreferences.contains(it.id)) {
                sharedLayerPreferences.getBoolean(it.id, true)
            } else {
                it.visibility.value == Property.VISIBLE
            }

            MapboxLayerSetting(it.id, visibility)
        }

    init {
        applyLayerSettings()
    }

    fun setBreadcrumbs(breadcrumbs: List<Breadcrumb>) {
        currentBreacrumbPolylines.forEach {
            mapboxMap.removePolyline(it)
        }

        val polylineOptions = mutableListOf<PolylineOptions>()
        breadcrumbs.sortedBy { it.dateCreated }?.forEach {
            if (it.startOfSession) {
                polylineOptions.add(PolylineOptions()
                        .color(R.color.greyHighlights))
            }

            polylineOptions.last().add(it.location.toMapboxLatLng())
        }

        val polylines = mutableListOf<Polyline>()
        polylineOptions.forEach { breadCrumbPath ->
            val polyline = mapboxMap.addPolyline(breadCrumbPath)
            polylines.add(polyline)
        }
        currentBreacrumbPolylines = polylines
        currentBreacrumbPolylineOptions = polylineOptions
    }

    fun addMarker(collectItem: CollectItem) {
        val marker = mapboxMap.addMarker(
                MarkerOptions()
                        .position(collectItem.location.toMapboxLatLng())
                        .title(collectItem.title)
                        .icon(MapboxIconFactory.getMapboxIcon(context, collectItem)))

        markers.add(
                Pair(
                        collectItem,
                        marker
                )
        )
    }

    fun addMarkerDiff(inputMarkers: List<CollectItem>) {
        (inputMarkers - markers.map {
            it.first
        }).forEach {
            addMarker(it)
        }
    }

    fun removeAllMarkers() {
        markers.forEach {
            it.second.remove()
        }
    }

    fun getCollectItem(forMarker: Marker): CollectItem? {
        return markers.filter { (item, marker) ->
            forMarker == marker
        }.map { (item, marker) ->
            item
        }.firstOrNull()
    }

    fun setCurrentLocation(latLng: LatLng, accuracy: Double) {
        removeCurrentLocationAttributes()
        addCurrentLocationAttributes(latLng, accuracy)
    }

    private fun addCurrentLocationAttributes(latLng: LatLng, accuracy: Double) {
        val polylineOptions = PolylineOptions()
                .color(R.color.colorPrimary)
        val points = mutableListOf<LatLng>()

        val start = calculateLatLng(latLng, accuracy, 0.0)
        points.add(start)

        val vertices = 360
        (1..vertices).forEach { it ->
            val degreesInRadians = Math.toRadians(it.toDouble() * DEGREES_IN_CIRCLE / vertices)
            points.add(calculateLatLng(latLng, accuracy, degreesInRadians))
        }

        points.add(start)
        polylineOptions.addAll(points)
        currentLocationPolyline = mapboxMap.addPolyline(polylineOptions)
        currentLocationMarker = mapboxMap.addMarker(MarkerOptions()
                .icon(userIcon)
                .position(latLng)
        )
    }

    private fun removeCurrentLocationAttributes() {
        currentLocationPolyline?.let {
            mapboxMap.removePolyline(it)
        }
        currentLocationMarker?.let {
            mapboxMap.removeMarker(it)
        }
    }

    fun getCurrentLocation(): LatLng? {
        return currentLocationMarker?.position
    }

    fun toggleBreadcrumbs() {
        showBreadcrumbs = !showBreadcrumbs

        if (showBreadcrumbs) {
            val newPolylines = mutableListOf<Polyline>()
            currentBreacrumbPolylineOptions.forEach {
                val polyline = mapboxMap.addPolyline(it)
                newPolylines.add(polyline)
            }
            currentBreacrumbPolylines = newPolylines
        } else {
            currentBreacrumbPolylines.forEach {
                mapboxMap.removePolyline(it)
            }
        }
    }

    companion object {
        private const val DEGREES_IN_CIRCLE = 360
        private const val EARTH_RADIUS_METERS = 6371000.0
        private const val PI_RADIANS_IN_DEGREES = 180

        private fun calculateLatLng(startPoint: LatLng, radiusInMeters: Double, degreeRadians: Double): LatLng {
            val distRadians = radiusInMeters / EARTH_RADIUS_METERS
            val centerLatRadians = startPoint.latitude * Math.PI / PI_RADIANS_IN_DEGREES
            val centerLonRadians = startPoint.longitude * Math.PI / PI_RADIANS_IN_DEGREES

            val pointLatRadians = Math.asin(sin(centerLatRadians) * cos(distRadians) + cos(centerLatRadians) * sin(distRadians) * cos(degreeRadians))
            val pointLonRadians = centerLonRadians + Math.atan2(sin(degreeRadians)
                    * sin(distRadians) * cos(centerLatRadians),
                    cos(distRadians) - sin(centerLatRadians) * sin(pointLatRadians))
            val pointLat = pointLatRadians * PI_RADIANS_IN_DEGREES / Math.PI
            val pointLon = pointLonRadians * PI_RADIANS_IN_DEGREES / Math.PI
            return LatLng(pointLat, pointLon)
        }

    }
}