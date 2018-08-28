package org.taskforce.episample.core.interfaces

interface LandmarkType {
    val name: String
    val iconLocation: String
    val metadata: LandmarkTypeMetadata
}

sealed class LandmarkTypeMetadata {
    class CustomId(val id: String) : LandmarkTypeMetadata()
    class BuiltInLandmark(val type: org.taskforce.episample.core.BuiltInLandmark) : LandmarkTypeMetadata()
}

data class LiveLandmarkType(override val name: String,
                       override val iconLocation: String,
                       override val metadata: LandmarkTypeMetadata) : LandmarkType