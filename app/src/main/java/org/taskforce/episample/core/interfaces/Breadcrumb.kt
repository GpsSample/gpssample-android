package org.taskforce.episample.core.interfaces

import com.google.android.gms.maps.model.LatLng
import java.util.*

interface Breadcrumb {
    val location: LatLng
    val gpsPrecision: Double
    val dateCreated: Date
}