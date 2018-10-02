package org.taskforce.episample.core.interfaces

interface UserSettings {
    val gpsMinimumPrecision: Double
    val gpsPreferredPrecision: Double
    val allowPhotos: Boolean
    val photoCompressionScale: Int?
}