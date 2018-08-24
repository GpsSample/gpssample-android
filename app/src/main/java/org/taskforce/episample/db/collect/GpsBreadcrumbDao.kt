package org.taskforce.episample.db.collect

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface GpsBreakcrumbDao {

    @Insert
    fun insert(gpsBreadcrumb: GpsBreadcrumb): Long

    @Query("SELECT * FROM gps_breadcrumb_table WHERE study_id LIKE :studyId")
    fun getBreadcrumbs(studyId: Long): LiveData<List<GpsBreadcrumb>>

    @Query("SELECT * FROM gps_breadcrumb_table WHERE study_id LIKE :studyId")
    fun getBreadcrumbsSync(studyId: Long): List<GpsBreadcrumb>
}