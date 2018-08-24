package org.taskforce.episample.collection.models

import com.google.android.gms.maps.model.LatLng

data class GpsBreadcrumb(
        private val collectorName: String,
        val latLng: LatLng)