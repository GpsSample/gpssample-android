package org.taskforce.episample.core.mock

import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.Landmark
import org.taskforce.episample.core.interfaces.LandmarkType
import java.util.*

data class MockLandmark(override val title: String?,
                        override val landmarkType: LandmarkType,
                        override val location: LatLng,
                        override val gpsPrecision: Double,
                        override val isIncomplete: Boolean,
                        override val image: String?,
                        override val note: String?,
                        override val dateCreated: Date = Date()): Landmark {
    companion object {
        fun createMockLandmark(title: String?, 
                               landmarkType: LandmarkType = MockLandmarkType.createMockLandmarkType(),
                               location: LatLng = LatLng(37.4211343, -122.0860752),
                               gpsPrecision: Double = 0.0,
                               isIncomplete: Boolean = false,
                               image: String? = null,
                               note: String? = null,
                               dateCreated: Date = Date()): MockLandmark {
            return MockLandmark(title, landmarkType, location, gpsPrecision, isIncomplete, image, note, dateCreated)
        }
    }
}