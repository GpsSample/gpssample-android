package org.taskforce.episample.core.interfaces

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import android.content.Context
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

interface LocationService : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun connectListener()

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun disconnectListener()

    val locationLiveData: MutableLiveData<Pair<LatLng, Float>>
    var locationServiceConfiguration: LocationServiceConfiguration
}

data class LocationServiceConfiguration(val interval: Long = 30000,
                                 val fastestInterval: Long = 5000,
                                 val priority: Int = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

class LiveLocationService(context: Context) : LocationService {

    override var locationServiceConfiguration = LocationServiceConfiguration()
    override val locationLiveData = MutableLiveData<Pair<LatLng, Float>>()

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)
    private val request: LocationRequest
        get() {
            val request = LocationRequest.create()
            request.interval = locationServiceConfiguration.interval
            request.fastestInterval = locationServiceConfiguration.fastestInterval
            request.priority = locationServiceConfiguration.priority
            return request
        }

    private val locationListener = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            result?.lastLocation?.let {
                locationLiveData.postValue(Pair(LatLng(it.latitude, it.longitude), it.accuracy))
            }
        }
    }

    @SuppressLint("MissingPermission")
    //    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    override fun connectListener() {
        locationClient.requestLocationUpdates(request, locationListener, null)
    }

    //    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun disconnectListener() {
        locationClient.removeLocationUpdates(locationListener)
    }
}