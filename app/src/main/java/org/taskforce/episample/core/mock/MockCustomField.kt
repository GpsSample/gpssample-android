package org.taskforce.episample.core.mock

import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.metadata.CustomFieldMetadata

class MockCustomField(override var name: String, 
                      override var type: CustomFieldType, 
                      override val isAutomatic: Boolean, 
                      override val isPrimary: Boolean, 
                      override var shouldExport: Boolean, 
                      override val isRequired: Boolean, 
                      override val isPersonallyIdentifiableInformation: Boolean, 
                      override val metadata: CustomFieldMetadata, 
                      override var configId: String, 
                      override var id: String) : CustomField {
    
    companion object {
        fun createMockCustomField(name: String,
                                  type: CustomFieldType = CustomFieldType.TEXT,
                                  isAutomatic: Boolean = false,
                                  isPrimary: Boolean = false,
                                  shouldExport: Boolean = false,
                                  isRequired: Boolean = false,
                                  isPersonallyIdentifiableInformation: Boolean = false,
                                  metadata: CustomFieldMetadata = MockCustomFieldMetadata(),
                                  configId: String = "",
                                  id: String = ""): MockCustomField {
            return MockCustomField(name, 
                    type, 
                    isAutomatic, 
                    isPrimary, 
                    shouldExport, 
                    isRequired, 
                    isPersonallyIdentifiableInformation, 
                    metadata, 
                    configId, 
                    id)
            
        }
    }
}