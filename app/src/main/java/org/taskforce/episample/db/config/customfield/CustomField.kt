package org.taskforce.episample.db.config.customfield

import android.arch.persistence.room.*
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.db.config.Config
import org.taskforce.episample.db.config.customfield.metadata.CustomFieldMetadata
import org.taskforce.episample.db.converter.CustomFieldMetadataConverter
import org.taskforce.episample.db.converter.CustomFieldTypeConverter
import java.util.*

@Entity(tableName = "custom_field_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Config::class, parentColumns = ["id"], childColumns = ["config_id"], onDelete = ForeignKey.CASCADE
            ))
        ])
@TypeConverters(CustomFieldMetadataConverter::class, CustomFieldTypeConverter::class)
class CustomField(
        override var name: String,
        override var type: CustomFieldType,
        @ColumnInfo(name = "is_automatic")
        override val isAutomatic: Boolean,
        @ColumnInfo(name = "is_primary")
        override val isPrimary: Boolean,
        @ColumnInfo(name = "should_export")
        override var shouldExport: Boolean,
        @ColumnInfo(name = "is_required")
        override val isRequired: Boolean,
        @ColumnInfo(name = "is_personally_identifiable_information")
        override val isPersonallyIdentifiableInformation: Boolean,
        @ColumnInfo(name = "custom_field_metadata")
        override val metadata: CustomFieldMetadata,
        @ColumnInfo(name = "config_id")
        override var configId: String,
        @PrimaryKey
        override var id: String = UUID.randomUUID().toString()) : CustomField