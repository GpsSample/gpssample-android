package org.taskforce.episample.core.mock

import org.taskforce.episample.core.interfaces.UserSettings

class MockUserSettings(override val gpsMinimumPrecision: Double,
                       override val gpsPreferredPrecision: Double): UserSettings {
    
    companion object {
        fun createMockUserSettings(minPrecision: Double = 20.0,
                                   preferredPrecision: Double = 4.0): MockUserSettings {
            return MockUserSettings(minPrecision, preferredPrecision)
        }
    }
}