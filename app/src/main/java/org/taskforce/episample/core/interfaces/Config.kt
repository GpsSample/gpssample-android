package org.taskforce.episample.core.interfaces

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
}