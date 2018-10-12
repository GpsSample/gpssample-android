package org.taskforce.episample.db.config

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.db.DateRange
import org.taskforce.episample.db.collect.*
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.CustomFieldValueDao
import org.taskforce.episample.db.config.landmark.CustomLandmarkType
import org.taskforce.episample.db.navigation.NavigationItem
import org.taskforce.episample.db.navigation.NavigationPlan
import org.taskforce.episample.db.sampling.SampleEntity
import org.taskforce.episample.db.sampling.SampleEnumerationEntity
import org.taskforce.episample.db.sampling.WarningEntity
import java.util.*

@Dao
abstract class StudyDao : ConfigDao(), CustomFieldDao, ResolvedEnumerationDao, CustomFieldValueDao {
    @Insert
    abstract fun insert(study: Study)

    @Insert
    abstract fun insert(enumeration: Enumeration)

    @Insert
    abstract fun insert(landmark: Landmark)

    @Insert
    abstract fun insert(breadcrumb: GpsBreadcrumb)

    @Insert
    abstract fun insert(sample: SampleEntity)

    @Insert
    abstract fun insert(vararg sampleEnumerationEntity: SampleEnumerationEntity)

    @Insert
    abstract fun insert(vararg sampleEnumerationEntity: WarningEntity)

    @Delete
    abstract fun delete(study: Study)

    @Insert
    abstract fun insertNavigationPlans(navigationPlans: List<NavigationPlan>)

    @Insert
    abstract fun insertNavigationItems(navigationItems: List<NavigationItem>)

    @Query("DELETE FROM samples")
    abstract fun deleteSamples()

    @Query("DELETE FROM navigation_plan_table")
    abstract fun deleteNavigationPlansSync()

    @Query("SELECT * FROM study_table")
    abstract fun getAllStudiesSync(): List<Study>

    @Query("SELECT * FROM study_table")
    abstract fun getAllStudies(): LiveData<List<Study>>

    @Query("SELECT * FROM study_table WHERE id LIKE :studyId")
    abstract fun getStudy(studyId: String): LiveData<Study>

    @Query("SELECT * FROM study_table WHERE id LIKE :studyId")
    abstract fun getStudySync(studyId: String): Study

    @Query("SELECT * FROM enumeration_table WHERE study_id LIKE :studyId")
    abstract fun getEnumerations(studyId: String): LiveData<List<ResolvedEnumeration>>

    @Query("SELECT et.id, lat, lng, note, is_excluded, is_complete, gps_precision, collector_name, title, image, date_created FROM enumeration_table et JOIN sample_enumerations se ON et.id = se.enumeration_id WHERE sample_id LIKE :sampleId")
    abstract fun getSampleResolvedEnumerationsSync(sampleId: String): List<ResolvedEnumeration>

    @Query("SELECT COUNT(id) FROM enumeration_table WHERE study_id LIKE :studyId AND is_complete = 0 AND is_excluded = 0") // is_complete = 0 is true. doh
    abstract fun getNumberOfValidEnumerations(studyId: String): LiveData<Int>

    @Query("SELECT COUNT(id) FROM samples WHERE study_id LIKE :studyId") // is_complete = 0 is true. doh
    abstract fun getNumberOfSamples(studyId: String): LiveData<Int>

    @Query("SELECT min(date_created) as min_date, max(date_created) as max_date FROM enumeration_table WHERE study_id LIKE :studyId AND is_complete = 0 AND is_excluded = 0") // is_complete = 0 is true. doh
    abstract fun getValidEnumerationsCollectionRange(studyId: String): LiveData<DateRange>

    @Query("SELECT * FROM enumeration_table WHERE study_id LIKE :studyId AND is_complete = 0 AND is_excluded = 0")
    abstract fun getValidEnumerationsSync(studyId: String): List<ResolvedEnumeration> // is_complete = 0 is true. doh

    @Query("SELECT * FROM enumeration_table WHERE study_id LIKE :studyId")
    abstract fun getEnumerationsSync(studyId: String): List<Enumeration>

    @Query("SELECT * from landmark_table WHERE study_id LIKE :studyId")
    abstract fun getLandmarks(studyId: String): LiveData<List<Landmark>>

    @Query("SELECT * from gps_breadcrumb_table WHERE study_id LIKE :studyId")
    abstract fun getBreadcrumbs(studyId: String): LiveData<List<GpsBreadcrumb>>

    @Query("SELECT * FROM custom_landmark_type_table WHERE config_id LIKE :configId")
    abstract fun getCustomLandmarkTypes(configId: String): LiveData<List<CustomLandmarkType>>

    @Query("SELECT * FROM sample_warnings sw JOIN samples st ON st.id = sw.sample_id WHERE study_id LIKE :studyId")
    abstract fun getWarnings(studyId: String): LiveData<List<WarningEntity>>

    @Query("SELECT * FROM samples WHERE study_id LIKE :studyId LIMIT 1")
    abstract fun getSample(studyId: String): LiveData<SampleEntity?>

    @Query("SELECT COUNT(et.id) FROM sample_enumerations et JOIN samples st ON et.sample_id = st.id WHERE study_id LIKE :studyId")
    abstract fun getNumberOfEnumerationsInSample(studyId: String): LiveData<Int>

    @Update
    abstract fun update(enumeration: Enumeration)

    @Update
    abstract fun update(landmark: Landmark)

    @Transaction
    open fun update(enumeration: Enumeration, customFieldValues: List<CustomFieldValue>) {
        update(enumeration)
        update(*customFieldValues.toTypedArray())
    }

    @Transaction
    open fun insert(enumeration: Enumeration, customFieldValues: List<CustomFieldValue>) {
        insert(enumeration)
        insert(*customFieldValues.toTypedArray())
    }

    @Transaction
    open fun insert(studyId: String, studyName: String, studyPassword: String, sourceConfig: ResolvedConfig): String {

        val insertConfig = Config(sourceConfig.name, sourceConfig.mapboxStyle.urlString, sourceConfig.mapMinZoom, sourceConfig.mapMaxZoom)

        val adminSettings = sourceConfig.adminSettings.apply {
            this.configId = insertConfig.id
        }
        val enumerationSubject = sourceConfig.enumerationSubject.apply {
            this.configId = insertConfig.id
        }
        val userSettings = sourceConfig.userSettings.apply {
            this.configId = insertConfig.id
        }
        val displaySettings = sourceConfig.displaySettings.apply {
            this.configId = insertConfig.id
        }

        val customFields = sourceConfig.customFields
        customFields.forEach {
            it.configId = insertConfig.id
        }

        val landmarkTypes = sourceConfig.customLandmarkTypes
        landmarkTypes.forEach {
            it.id = UUID.randomUUID().toString()
            it.configId = insertConfig.id
        }

        val methodology = sourceConfig.methodology.apply {
            configId = insertConfig.id
        }.unresolved
        val ruleSets = sourceConfig.methodology.ruleSets.map { it.unresolved }
        val ruleRecords = sourceConfig.methodology.ruleSets.flatMap { it.ruleRecords }.map { it.unresolved }

        val enumerationAreas = sourceConfig.enumerationAreas
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

        insert(insertConfig,
                customFields,
                landmarkTypes,
                adminSettings,
                enumerationSubject,
                userSettings,
                displaySettings,
                methodology,
                ruleSets,
                ruleRecords,
                insertEnumerationAreas,
                enumerationAreaPoints)
        insert(Study(studyName, studyPassword, configId = insertConfig.id, id = studyId))

        return insertConfig.id
    }
}