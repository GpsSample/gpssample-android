package org.taskforce.episample.core.interfaces

import com.google.android.gms.maps.model.LatLng
import java.util.*

interface CapturedLocation {
    val location: LatLng
    val gpsPrecision: Double
    val collectorName: String
    val dateCreated: Date
}

interface Breadcrumb: CapturedLocation {
    val startOfSession: Boolean
}

data class LiveBreadcrumb(override val collectorName: String,
                          override val location: LatLng,
                          override val gpsPrecision: Double,
                          override val startOfSession: Boolean,
                          override val dateCreated: Date): Breadcrumb
