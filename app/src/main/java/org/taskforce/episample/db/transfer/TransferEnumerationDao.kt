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

    @Query("SELECT * from enumeration_table")
    fun getEnumerations(): List<Enumeration>
}
