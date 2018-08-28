package org.taskforce.episample.core.mock

import org.taskforce.episample.core.BuiltInLandmark
import org.taskforce.episample.core.interfaces.LandmarkType
import org.taskforce.episample.core.interfaces.LandmarkTypeMetadata

data class MockLandmarkType(override val name: String,
                            override val iconLocation: String = "android.resource://org.taskforce.episample/2131165276",
                            override val metadata: LandmarkTypeMetadata): LandmarkType {
    companion object {
        fun createMockLandmarkType(name: String = "Default", iconLocation: String = "android.resource://org.taskforce.episample/2131165276"): MockLandmarkType {
            return MockLandmarkType(name, iconLocation, LandmarkTypeMetadata.BuiltInLandmark(BuiltInLandmark.DEFAULT))
        }
    }
}