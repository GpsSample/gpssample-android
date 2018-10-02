package org.taskforce.episample.db.transfer

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import org.taskforce.episample.db.collect.GpsBreadcrumb

@Dao
interface TransferBreadcrumbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBreadcrumbs(breadcrumbs: List<GpsBreadcrumb>)

    @Query("SELECT * from gps_breadcrumb_table")
    fun getBreadcrumbs(): List<GpsBreadcrumb>
}