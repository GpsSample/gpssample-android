package org.taskforce.episample.core.interfaces

import org.taskforce.episample.config.sampling.ResolvedSamplingMethodEntity
import org.taskforce.episample.core.models.MapboxStyleUrl
import java.util.*

interface Config {
    val name: String
    val dateCreated: Date
    val id: String
    val adminSettings: AdminSettings
    val userSettings: UserSettings
    val displaySettings: DisplaySettings
    val enumerationSubject: EnumerationSubject
    val customFields: List<CustomField>
    val landmarkTypes: List<LandmarkType>
    val enumerationAreas: List<EnumerationArea>
    val methodology: ResolvedSamplingMethodEntity
    val mapboxStyle: MapboxStyleUrl
}

