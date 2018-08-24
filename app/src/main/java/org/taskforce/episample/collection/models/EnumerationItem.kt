package org.taskforce.episample.collection.models

import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.config.fields.CustomFieldDataItem

class EnumerationItem(
        location: LatLng,
        val precision: Double,
        val isLocationCopied: Boolean,
        isIncomplete: Boolean,
        val isExcluded: Boolean,
        val notes: String?,
        val data: Map<String, CustomFieldDataItem>) :
        CollectItem(
                location,
                isIncomplete,
                data.values.firstOrNull {
                        it.isPrimary
                }?.data as String? ?: "Unknown"
        )
