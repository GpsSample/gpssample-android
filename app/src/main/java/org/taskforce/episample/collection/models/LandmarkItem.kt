package org.taskforce.episample.collection.models

import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.fileImport.models.LandmarkType

class LandmarkItem(
        location: LatLng,
        val type: LandmarkType,
        title: String?,
        val notes: String?) :
        CollectItem(location, false, title)
