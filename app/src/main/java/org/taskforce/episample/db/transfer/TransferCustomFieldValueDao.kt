package org.taskforce.episample.db.transfer

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import org.taskforce.episample.db.config.customfield.CustomFieldValue

@Dao
interface TransferCustomFieldValueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomFieldValues(customFieldValues: List<CustomFieldValue>)

    @Query("SELECT * from custom_field_value_table")
    fun getCustomFieldValues(): List<CustomFieldValue>
}