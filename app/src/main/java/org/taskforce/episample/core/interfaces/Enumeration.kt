package org.taskforce.episample.core.interfaces

import com.google.android.gms.maps.model.LatLng
import java.util.*

interface Enumeration : CollectItem {
    val isIncomplete: Boolean
    val customFieldValues: List<CustomFieldValue>
    val isExcluded: Boolean
}

class LiveEnumeration(override val image: String?,
                      override val isIncomplete: Boolean,
                      override val isExcluded: Boolean,
                      override val title: String?,
                      override val note: String?,
                      override val location: LatLng,
                      override val gpsPrecision: Double,
                      override val displayDate: String,
                      override val customFieldValues: List<CustomFieldValue>,
                      override val id: String?,
                      override val dateCreated: Date = Date()) : Enumeration
