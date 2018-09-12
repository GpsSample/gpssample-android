package org.taskforce.episample.core.interfaces

import com.google.android.gms.maps.model.LatLng
import java.util.*

interface Landmark : CollectItem {
    override val title: String
    val landmarkType: LandmarkType
}

class LiveLandmark(override val title: String,
                   override val landmarkType: LandmarkType,
                   override val note: String?,
                   override val image: String?,
                   override val location: LatLng,
                   override val gpsPrecision: Double,
                   override val id: String?,
                   override val dateCreated: Date = Date()) : Landmark