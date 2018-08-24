package org.taskforce.episample.core.interfaces

import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.metadata.CustomFieldMetadata

interface CustomField {
    var name: String
    var type: CustomFieldType
    val isAutomatic: Boolean
    val isPrimary: Boolean
    var shouldExport: Boolean
    val isRequired: Boolean
    val isPersonallyIdentifiableInformation: Boolean
    val metadata: CustomFieldMetadata
    var configId: String
    var id: String
}