package org.taskforce.episample.db.config

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.db.collect.*
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.landmark.CustomLandmarkType

@Dao
abstract class StudyDao : ConfigDao(), CustomFieldDao, ResolvedEnumerationDao {

    @Insert
    abstract fun insert(study: Study)

    @Insert
    abstract fun insert(enumeration: Enumeration)

    @Insert
    abstract fun insert(landmark: Landmark)

    @Insert
    abstract fun insert(breadcrumb: GpsBreadcrumb)

    @Delete
    abstract fun delete(study: Study)

    @Query("SELECT * FROM study_table")
    abstract fun getAllStudiesSync(): List<Study>

    @Query("SELECT * FROM study_table WHERE id LIKE :studyId")
    abstract fun getStudy(studyId: String): LiveData<Study>

    @Query("SELECT * FROM study_table WHERE id LIKE :studyId")
    abstract fun getStudySync(studyId: String): Study

    @Query("SELECT * FROM enumeration_table WHERE study_id LIKE :studyId")
    abstract fun getEnumerations(studyId: String): LiveData<List<ResolvedEnumeration>>

    @Query("SELECT * FROM enumeration_table WHERE study_id LIKE :studyId")
    abstract fun getEnumerationsSync(studyId: String): List<Enumeration>

    @Query("SELECT * from landmark_table WHERE study_id LIKE :studyId")
    abstract fun getLandmarks(studyId: String): LiveData<List<Landmark>>

    @Query("SELECT * from gps_breadcrumb_table WHERE study_id LIKE :studyId")
    abstract fun getBreadcrumbs(studyId: String): LiveData<List<GpsBreadcrumb>>

    @Query("SELECT * FROM custom_landmark_type_table WHERE config_id LIKE :configId")
    abstract fun getCustomLandmarkTypes(configId: String): LiveData<List<CustomLandmarkType>>

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
    open fun insert(study: Study, sourceConfigId: String): String {
        insert(study)

        val sourceConfig = getConfigSync(sourceConfigId)

        return duplicate(sourceConfig, sourceConfig.name, study.id)
    }
}