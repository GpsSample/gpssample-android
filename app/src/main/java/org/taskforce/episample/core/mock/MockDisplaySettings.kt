package org.taskforce.episample.core.mock

import org.taskforce.episample.core.interfaces.DisplaySettings

class MockDisplaySettings(override val isMetricDate: Boolean,
                          override val is24HourTime: Boolean): DisplaySettings {
    
    companion object {
        fun createMockDisplaySettings(isMetricDate: Boolean = true,
                                      is24HourTime: Boolean = true): DisplaySettings {
            return MockDisplaySettings(isMetricDate, is24HourTime)
        }
    }
}