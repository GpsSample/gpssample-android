package org.taskforce.episample.core.interfaces

import java.util.*

interface Config {
    var name: String
    val dateCreated: Date
    val id: String
    var adminSettings: AdminSettings
    var enumerationSubject: EnumerationSubject
    var customFields: List<CustomField>
}