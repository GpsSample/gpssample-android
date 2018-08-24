package org.taskforce.episample.db.config

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.converter.DateConverter
import java.util.*

@TypeConverters(DateConverter::class)
class ResolvedStudy(
        var id: String,
        var name: String,
        @ColumnInfo(name = "date_created")
        val dateCreated: Date) {
    @Relation(parentColumn = "id", entityColumn = "study_id")
    var enumerations: List<Enumeration> = listOf()

    @Relation(parentColumn = "id", entityColumn = "study_id")
    var breadcrumbs: List<GpsBreadcrumb> = listOf()
}

@Dao
abstract class ResolvedStudyDao {
    @Transaction
    @Query("SELECT * from study_table WHERE id LIKE :studyId")
    abstract fun getStudy(studyId: String): LiveData<ResolvedStudy>

    @Transaction
    @Query("SELECT * from study_table WHERE id LIKE :studyId")
    abstract fun getStudySync(studyId: String): ResolvedStudy
}