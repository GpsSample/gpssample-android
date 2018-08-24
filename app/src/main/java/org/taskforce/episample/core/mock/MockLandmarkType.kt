package org.taskforce.episample.core.mock

import org.taskforce.episample.core.interfaces.LandmarkType

data class MockLandmarkType(override val name: String,
                            override val iconLocation: String = "android.resource://org.taskforce.episample/2131165276"): LandmarkType {
    companion object {
        fun createMockLandmarkType(name: String = "Default", iconLocation: String = "android.resource://org.taskforce.episample/2131165276"): MockLandmarkType {
            return MockLandmarkType(name, iconLocation)
        }
    }
}