package org.taskforce.episample.db.config

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.landmark.CustomLandmarkType
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.db.sampling.strata.Strata
import org.taskforce.episample.db.sampling.subsets.Subset
import java.util.*

@Dao
abstract class ConfigDao : CustomFieldDao, EnumerationAreaDao {

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

    @Query("SELECT * FROM strata_table WHERE config_id LIKE :configId")
    abstract fun getStrataByConfigSync(configId: String): List<Strata>

    @Query("SELECT * FROM subset_table WHERE config_id LIKE :configId")
    abstract fun getSubsetsByConfigSync(configId: String): List<Subset>

    @Query("SELECT * FROM rule_set_table JOIN strata_table ON rule_set_id WHERE config_id LIKE :configId")
    abstract fun getStrataRuleSetsByConfigSync(configId: String): List<RuleSet>

    @Query("SELECT * FROM rule_set_table JOIN subset_table ON rule_set_id WHERE config_id LIKE :configId")
    abstract fun getSubsetRuleSetsByConfigSync(configId: String): List<RuleSet>

    @Query("SELECT * FROM rule_record_table JOIN rule_set_table rst ON rst.id JOIN strata_table st ON st.rule_set_id WHERE config_id LIKE :configId")
    abstract fun getStrataRuleRecordsByConfigSync(configId: String): List<RuleRecord>

    @Query("SELECT * FROM rule_record_table JOIN rule_set_table rst ON rst.id JOIN subset_table st ON st.rule_set_id WHERE config_id LIKE :configId")
    abstract fun getSubsetRuleRecordsByConfigSync(configId: String): List<RuleRecord>

    @Query("SELECT * FROM custom_landmark_type_table WHERE config_id LIKE :configId")
    abstract fun getLandmarkTypesByConfigSync(configId: String): List<CustomLandmarkType>

    @Insert
    abstract fun insert(vararg ruleSet: RuleSet)

    @Insert
    abstract fun insert(vararg ruleSet: RuleRecord)

    @Insert
    abstract fun insert(vararg ruleSet: Strata)

    @Insert
    abstract fun insert(vararg ruleSet: Subset)

    @Transaction
    open fun insert(config: Config,
                    customFields: List<CustomField>,
                    landmarkTypes: List<CustomLandmarkType>,
                    adminSettings: AdminSettings?,
                    enumerationSubject: EnumerationSubject?,
                    userSettings: UserSettings?,
                    displaySettings: DisplaySettings?,
                    strata: List<Strata>,
                    subsets: List<Subset>,
                    ruleSets: List<RuleSet>,
                    ruleRecords: List<RuleRecord>,
                    enumerationAreas: List<EnumerationArea>,
                    enumerationAreaPoints: List<EnumerationAreaPoint>) {
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

        insert(*ruleSets.toTypedArray())
        insert(*ruleRecords.toTypedArray())
        insert(*strata.toTypedArray())
        insert(*subsets.toTypedArray())

        insertEnumerationAreas(*enumerationAreas.toTypedArray())
        insertEnumerationAreaPoints(*enumerationAreaPoints.toTypedArray())
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
        
        val enumerationAreas = getEnumerationAreasSync(sourceConfig.id)
        enumerationAreas.forEach { 
            val id = UUID.randomUUID().toString()
            it.id = id
            it.configId = insertConfig.id
            
            it.points.forEach { 
                it.enumerationAreaId = id
                it.id = UUID.randomUUID().toString()
            }
        }
        
        val enumerationAreaPoints = enumerationAreas.map { area ->
            area.points
        }.flatMap {
            it
        }
        
        val insertEnumerationAreas = enumerationAreas.map { 
            EnumerationArea(it.name, it.configId, it.id)
        }


        //TODO review by someone who has better knowledge of sync
        val strataRuleRecords = getStrataRuleRecordsByConfigSync(sourceConfig.id)
        val subsetRuleRecords = getSubsetRuleRecordsByConfigSync(sourceConfig.id)
        val strata = getStrataByConfigSync(sourceConfig.id).apply {
            forEach {
                it.configId = insertConfig.id
            }
        }
        val strataRuleSets = getStrataRuleSetsByConfigSync(sourceConfig.id)
        val subsets = getSubsetsByConfigSync(sourceConfig.id).apply {
            forEach {
                it.configId = insertConfig.id
            }
        }
        val subsetRuleSets = getSubsetRuleSetsByConfigSync(sourceConfig.id)

        val ruleSets = strataRuleSets.toMutableList().apply { addAll(subsetRuleSets) }.toList()
        val ruleRecords = strataRuleRecords.toMutableList().apply { addAll(subsetRuleRecords) }.toList()

        insert(insertConfig,
                customFields,
                landmarkTypes,
                adminSettings,
                enumerationSubject,
                userSettings,
                displaySettings,
                strata,
                subsets,
                ruleSets,
                ruleRecords,
                insertEnumerationAreas,
                enumerationAreaPoints)

        return insertConfig.id
    }
}