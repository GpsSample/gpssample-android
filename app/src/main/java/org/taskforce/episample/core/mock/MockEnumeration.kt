package org.taskforce.episample.core.mock

import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.CustomFieldValue
import org.taskforce.episample.core.interfaces.Enumeration
import java.util.*

data class MockEnumeration(override val title: String?,
                           override val location: LatLng,
                           override val gpsPrecision: Double,
                           override val isIncomplete: Boolean,
                           override val image: String?,
                           override val note: String?,
                           override val dateCreated: Date = Date()): Enumeration {

    override val customFieldValues: List<CustomFieldValue> = listOf()

    override val displayDate: String = "TODO"

    companion object {
        fun createMockEnumeration(title: String, 
                                  location: LatLng = LatLng(37.4211343, -122.0860752),
                                  gpsPrecision: Double = 0.0,
                                  isIncomplete: Boolean = false,
                                  image: String? = null,
                                  note: String? = null,
                                  dateCreated: Date = Date()): MockEnumeration {
            return MockEnumeration(title, location, gpsPrecision, isIncomplete, image, note, dateCreated)
        }
    }
}