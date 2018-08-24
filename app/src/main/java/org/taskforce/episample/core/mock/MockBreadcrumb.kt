package org.taskforce.episample.core.mock

import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.Breadcrumb
import java.util.*

data class MockBreadcrumb(override val gpsPrecision: Double,
                          override val location: LatLng,
                          override val dateCreated: Date = Date()): Breadcrumb {
    companion object {
        fun createMockBreadcrumb(gpsPrecision: Double = 0.0,
                                 location: LatLng = LatLng(37.4211343, -122.0860752),
                                 dateCreated: Date = Date()): MockBreadcrumb {
            return MockBreadcrumb(gpsPrecision, location, dateCreated)
        }
    }
}