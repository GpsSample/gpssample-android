package org.taskforce.episample

import junit.framework.Assert.assertEquals
import org.junit.Test
import org.taskforce.episample.config.fields.CustomFieldTypeConstants
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.metadata.NumberMetadata
import org.taskforce.episample.utils.makeDBConfig
import java.util.*

class CustomFieldTest {
    @Test
    fun customFieldConversion() {
        val configId = UUID.randomUUID().toString()
        val expectedDoubleFieldValues = CustomField(
                name = "Number Field Name",
                type = CustomFieldType.NUMBER,
                isAutomatic = true,
                isPrimary = false,
                shouldExport = true,
                isPersonallyIdentifiableInformation = false,
                metadata = NumberMetadata(isIntegerOnly = false),
                configId = configId,
                isRequired = true)
        val expectedIntFieldValues = CustomField(
                name = "Number Field Name",
                type = CustomFieldType.NUMBER,
                isAutomatic = false,
                isPrimary = true,
                shouldExport = false,
                isPersonallyIdentifiableInformation = true,
                metadata = NumberMetadata(isIntegerOnly = true),
                configId = configId,
                isRequired = false)
        val customFields = listOf(
                expectedDoubleFieldValues.makeCustomField(mapOf(
                        CustomFieldTypeConstants.INTEGER_ONLY to false
                )),
                expectedIntFieldValues.makeCustomField(mapOf(
                        CustomFieldTypeConstants.INTEGER_ONLY to true
                ))
        )

        val convertedFields = customFields.map {
            it.makeDBConfig(configId)
        }

        assertEquals(2, convertedFields?.size)

        val intType = convertedFields.first { it.type == CustomFieldType.NUMBER && (it.metadata as NumberMetadata).isIntegerOnly }
        assertEquals(true, (intType.metadata as NumberMetadata).isIntegerOnly)
        assertEquals(expectedIntFieldValues.name, intType.name)
        assertEquals(expectedIntFieldValues.type, intType.type)
        assertEquals(expectedIntFieldValues.isAutomatic, intType.isAutomatic)
        assertEquals(expectedIntFieldValues.isPrimary, intType.isPrimary)
        assertEquals(expectedIntFieldValues.shouldExport, intType.shouldExport)
        assertEquals(expectedIntFieldValues.isPersonallyIdentifiableInformation, intType.isPersonallyIdentifiableInformation)
        assertEquals(expectedIntFieldValues.configId, intType.configId)
        assertEquals(expectedIntFieldValues.isRequired, intType.isRequired)
    }

    companion object {
        fun CustomField.makeCustomField(properties: Map<String, Any>): org.taskforce.episample.config.fields.CustomField {
            return org.taskforce.episample.config.fields.CustomField(
                    isAutomatic,
                    isPrimary,
                    shouldExport,
                    isRequired,
                    isPersonallyIdentifiableInformation,
                    name,
                    type,
                    properties,
                    id)
        }
    }
}
