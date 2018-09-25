package org.taskforce.episample.db.config

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.landmark.CustomLandmarkType
import org.taskforce.episample.db.filter.RuleSet
import java.util.*

@Dao
abstract class ConfigDao : CustomFieldDao {

    @Insert
    abstract fun insert(config: Config)

    @Delete
    abstract fun delete(config: Config)

    @Query("DELETE FROM config_table")
    abstract fun deleteAll()

    @Query("SELECT * FROM config_table ORDER BY name ASC")
    abstract fun getAllConfigs(): LiveData<List<Config>>

    @Query("SELECT * FROM config_table ORDER BY name ASC")
    abstract fun getAllConfigsSync(): List<Config>

    @Query("SELECT * FROM config_table WHERE id LIKE :configId")
    abstract fun getConfig(configId: String): LiveData<Config>

    @Query("SELECT * FROM config_table WHERE id LIKE :configId")
    abstract fun getConfigSync(configId: String): Config

    @Insert
    abstract fun insert(adminSettings: AdminSettings)

    @Query("SELECT * FROM admin_settings_table WHERE admin_settings_config_id LIKE :configId")
    abstract fun getAdminSettingsSync(configId: String): AdminSettings?

    @Insert
    abstract fun insert(enumerationSubject: EnumerationSubject)

    @Insert
    abstract fun insert(customField: CustomField)

    @Insert
    abstract fun insert(userSettings: UserSettings)

    @Insert
    abstract fun insert(displaySettings: DisplaySettings)

    @Insert
    abstract fun insert(vararg customField: CustomField)

    @Insert
    abstract fun insert(vararg customLandmarkType: CustomLandmarkType)

    @Query("SELECT * FROM enumeration_subject_table WHERE enumeration_subject_config_id LIKE :configId")
    abstract fun getEnumerationSubjectSync(configId: String): EnumerationSubject?

    @Query("SELECT * FROM user_settings_table WHERE user_settings_config_id LIKE :configId")
    abstract fun getUserSettingsSync(configId: String): UserSettings?

    @Query("SELECT * FROM display_settings_table WHERE display_settings_config_id LIKE :configId")
    abstract fun getDisplaySettingsSync(configId: String): DisplaySettings?

    @Query("SELECT * FROM custom_landmark_type_table WHERE config_id LIKE :configId")
    abstract fun getLandmarkTypesByConfigSync(configId: String): List<CustomLandmarkType>

    @Insert
    abstract fun insert(ruleSet: RuleSet)

    @Transaction
    open fun insert(config: Config,
                    customFields: List<CustomField>,
                    landmarkTypes: List<CustomLandmarkType>,
                    adminSettings: AdminSettings?,
                    enumerationSubject: EnumerationSubject?,
                    userSettings: UserSettings?, displaySettings: DisplaySettings?) {
        insert(config)

        adminSettings?.let {
            insert(it)
        }
        enumerationSubject?.let {
            insert(it)
        }
        userSettings?.let {
            insert(it)
        }
        displaySettings?.let {
            insert(it)
        }

        insert(*customFields.toTypedArray())
        insert(*landmarkTypes.toTypedArray())
    }

    @Transaction
    open fun duplicate(sourceConfig: Config, newName: String): String {
        val insertConfig = Config(newName)

        val adminSettings = getAdminSettingsSync(sourceConfig.id)?.apply {
            this.configId = insertConfig.id
        }
        val enumerationSubject = getEnumerationSubjectSync(sourceConfig.id)?.apply {
            this.configId = insertConfig.id
        }
        val userSettings = getUserSettingsSync(sourceConfig.id)?.apply {
            this.configId = insertConfig.id
        }
        val displaySettings = getDisplaySettingsSync(sourceConfig.id)?.apply {
            this.configId = insertConfig.id
        }

        val customFields = getFieldsByConfigSync(sourceConfig.id)
        customFields.forEach {
            it.id = UUID.randomUUID().toString()
            it.configId = insertConfig.id
        }

        val landmarkTypes = getLandmarkTypesByConfigSync(sourceConfig.id)
        landmarkTypes.forEach {
            it.id = UUID.randomUUID().toString()
            it.configId = insertConfig.id
        }

        insert(insertConfig,
                customFields,
                landmarkTypes,
                adminSettings,
                enumerationSubject,
                userSettings,
                displaySettings)

        return insertConfig.id
    }
}