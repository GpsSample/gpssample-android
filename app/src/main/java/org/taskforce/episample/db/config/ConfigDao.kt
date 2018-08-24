package org.taskforce.episample.db.config

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldDao
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
    abstract fun insert(vararg customField: CustomField)

    @Query("SELECT * FROM enumeration_subject_table WHERE enumeration_subject_config_id LIKE :configId")
    abstract fun getEnumerationSubjectSync(configId: String): EnumerationSubject?

    @Transaction
    open fun insert(config: Config, customFields: List<CustomField>, adminSettings: AdminSettings?, enumerationSubject: EnumerationSubject?) {
        insert(config)
        if (adminSettings != null) {
            insert(adminSettings)
        }
        if (enumerationSubject != null) {
            insert(enumerationSubject)
        }
        insert(*customFields.toTypedArray())
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

        val customFields = getFieldsByConfigSync(insertConfig.id)
        customFields.forEach {
            it.id = UUID.randomUUID().toString()
            it.configId = insertConfig.id
        }

        insert(insertConfig,
                customFields,
                adminSettings,
                enumerationSubject)

        return insertConfig.id
    }
}