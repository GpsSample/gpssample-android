package org.taskforce.episample.core.interfaces

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng

interface LocationService: LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun connectListener()

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun disconnectListener()

    val collectorName: String
    var collectManager: CollectManager?
    var configuration: LocationServiceConfiguration
    val locationLiveData: MutableLiveData<Pair<LatLng, Float>>
}
data class LocationServiceConfiguration(val minTime: Long = 15000,
                                        val minDistance: Float = 0f)

class LiveLocationService(context: Context,
                          override val collectorName: String,
                          val userSettings: UserSettings) : LocationService {

    override var collectManager: CollectManager? = null

    override var configuration: LocationServiceConfiguration = LocationServiceConfiguration()
        @SuppressLint("MissingPermission")
        set(value) {
            field = value
            locationClient.removeUpdates(listener)
            locationClient.requestLocationUpdates(LocationManager.GPS_PROVIDER, configuration.minTime, configuration.minDistance, listener)
        }

    override val locationLiveData = MutableLiveData<Pair<LatLng, Float>>()

    private val locationClient = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val listener = object: LocationListener {
        override fun onLocationChanged(location: Location) {
            val latLng = LatLng(location.latitude, location.longitude)
            locationLiveData.postValue(latLng to location.accuracy)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // no-op
        }

        override fun onProviderEnabled(provider: String?) {
            // no-op
        }

        override fun onProviderDisabled(provider: String?) {
            // no-op
        }
    }

    @SuppressLint("MissingPermission")
    override fun connectListener() {
        locationClient.requestLocationUpdates(LocationManager.GPS_PROVIDER, configuration.minTime, configuration.minDistance, listener)
    }

    override fun disconnectListener() {
        locationClient.removeUpdates(listener)
    }
}