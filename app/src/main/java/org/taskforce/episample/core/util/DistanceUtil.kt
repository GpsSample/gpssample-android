package org.taskforce.episample.core.util

class DistanceUtil {
    
    companion object {
        fun convertMetersToString(meters: Float): String {
            val kilometers = meters / 1000
            var length = kilometers
            var unit = "km"
            if (meters < 1000) {
                length = meters
                unit = "m"
            }

            return String.format("%.1f $unit away", length)
        }
    }
}