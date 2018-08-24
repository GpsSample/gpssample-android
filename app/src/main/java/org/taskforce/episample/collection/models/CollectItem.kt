package org.taskforce.episample.collection.models

import com.google.android.gms.maps.model.LatLng
import java.util.*

abstract class CollectItem(
        val location: LatLng,
        val isIncomplete: Boolean,
        val title: String?,
        val id: String = UUID.randomUUID().toString(),
        val dateCreated: Date = Date()) {

    val displayDate: String
        get() = dateCreated.toString()

}