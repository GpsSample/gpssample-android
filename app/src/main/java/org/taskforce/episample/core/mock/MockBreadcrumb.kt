package org.taskforce.episample.core.mock

import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.Breadcrumb
import java.util.*

data class MockBreadcrumb(override val collectorName: String,
                          override val gpsPrecision: Double,
                          override val location: LatLng,
                          override val startOfSession: Boolean,
                          override val dateCreated: Date = Date()) : Breadcrumb {

    companion object {
        fun createMockBreadcrumb(collectorName: String = "Collector Name",
                                 gpsPrecision: Double = 0.0,
                                 location: LatLng = LatLng(37.4211343, -122.0860752),
                                 startOfSession: Boolean,
                                 dateCreated: Date = Date()): MockBreadcrumb {
            return MockBreadcrumb(collectorName, gpsPrecision, location, startOfSession, dateCreated)
        }
    }
}