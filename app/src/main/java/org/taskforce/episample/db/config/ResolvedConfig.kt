package org.taskforce.episample.db.config

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.landmark.CustomLandmarkType
import org.taskforce.episample.db.converter.DateConverter
import org.taskforce.episample.db.filter.RuleSet
import java.util.*

@TypeConverters(DateConverter::class)
class ResolvedConfig(var name: String,
                     @ColumnInfo(name = "date_created")
                     val dateCreated: Date = Date(),
                     val id: String) {

    @Embedded()
    lateinit var adminSettings: AdminSettings

    @Embedded()
    lateinit var enumerationSubject: EnumerationSubject

    @Embedded()
    lateinit var userSettings: UserSettings

    @Embedded()
    lateinit var displaySettings: DisplaySettings

    @Relation(parentColumn = "id", entityColumn = "config_id")
    lateinit var customFields: List<CustomField>

    @Relation(parentColumn = "id", entityColumn = "config_id")
    lateinit var customLandmarkTypes: List<CustomLandmarkType>

    @Relation(parentColumn = "id", entityColumn = "config_id")
    lateinit var ruleSets: List<RuleSet>
}

@Dao
abstract class ResolvedConfigDao {
    @Query("SELECT * from config_table c INNER JOIN display_settings_table ds ON ds.display_settings_config_id = c.id INNER JOIN user_settings_table us ON us.user_settings_config_id = c.id INNER JOIN admin_settings_table a ON a.admin_settings_config_id = c.id INNER JOIN enumeration_subject_table e ON e.enumeration_subject_config_id = c.id WHERE c.id LIKE :configId")
    abstract fun getConfig(configId: String): LiveData<ResolvedConfig>

    @Query("SELECT * from config_table c INNER JOIN display_settings_table ds ON ds.display_settings_config_id = c.id INNER JOIN user_settings_table us ON us.user_settings_config_id = c.id INNER JOIN admin_settings_table a ON a.admin_settings_config_id = c.id INNER JOIN enumeration_subject_table e ON e.enumeration_subject_config_id = c.id WHERE c.id LIKE :configId")
    abstract fun getConfigSync(configId: String): List<ResolvedConfig>
}