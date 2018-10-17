package org.taskforce.episample.db.transfer

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import org.taskforce.episample.db.collect.Enumeration

@Dao
interface TransferEnumerationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEnumerations(enumerations: List<Enumeration>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnoringConflicts(enumerations: List<Enumeration>)

    @Query("SELECT * from enumeration_table WHERE note IS NOT NULL OR title IS NOT NULL OR image IS NOT NULL OR is_deleted = 1")
    fun getNonNullOrDeletedEnumerations(): List<Enumeration>

    @Query("SELECT * from enumeration_table WHERE is_deleted = 1")
    fun getDeletedEnumerations(): List<Enumeration>

    @Query("SELECT * from enumeration_table WHERE note IS NULL AND title IS NULL AND image IS NULL AND is_deleted = 0")
    fun getNullEnumerations(): List<Enumeration>

    @Query("SELECT * from enumeration_table")
    fun getAllEnumerations(): List<Enumeration>
}
