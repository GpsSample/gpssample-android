package org.taskforce.episample.db.transfer

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.Landmark

@Dao
interface TransferLandmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLandmarks(landmarks: List<Landmark>)

    @Query("SELECT * from landmark_table WHERE is_deleted = 1")
    fun getDeletedLandmarks(): List<Landmark>

    @Query("SELECT * from landmark_table")
    fun getAllLandmarks(): List<Landmark>
}