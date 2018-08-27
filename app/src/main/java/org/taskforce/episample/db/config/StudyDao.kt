package org.taskforce.episample.db.config

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.collect.ResolvedEnumeration
import java.util.*

@Dao
abstract class StudyDao : ConfigDao() {

    @Insert
    abstract fun insert(study: Study)

    @Insert
    abstract fun insert(enumeration: Enumeration)

    @Insert
    abstract fun insert(breadcrumb: GpsBreadcrumb)

    @Delete
    abstract fun delete(study: Study)

    @Query("SELECT * FROM study_table WHERE id LIKE :studyId")
    abstract fun getStudy(studyId: String): LiveData<Study>

    @Query("SELECT * FROM study_table WHERE id LIKE :studyId")
    abstract fun getStudySync(studyId: String): Study

    @Query("SELECT * FROM enumeration_table WHERE study_id LIKE :studyId")
    abstract fun getEnumerations(studyId: String): LiveData<List<Enumeration>>

    @Transaction
    open fun insert(study: Study, insertConfig: Config, sourceConfigId: String) {
        insert(study)

        val adminSettings = getAdminSettingsSync(sourceConfigId)?.apply {
            this.configId = insertConfig.id
        }
        val enumerationSubject = getEnumerationSubjectSync(sourceConfigId)?.apply {
            this.configId = insertConfig.id
        }
        val customFields = getFieldsByConfigSync(sourceConfigId)
        customFields.forEach {
            it.id = UUID.randomUUID().toString()
            it.configId = insertConfig.id
        }

        insert(
                insertConfig,
                customFields,
                adminSettings,
                enumerationSubject
        )
    }

    @Query("SELECT * FROM enumeration_table WHERE study_id LIKE :studyId ")
    abstract fun getResolvedEnumerationsSync(studyId: String): List<ResolvedEnumeration>
}