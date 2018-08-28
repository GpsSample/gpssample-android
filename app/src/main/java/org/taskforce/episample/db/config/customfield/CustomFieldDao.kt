package org.taskforce.episample.db.config.customfield

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

@Dao
interface CustomFieldDao {
    @Insert
    fun insert(customFieldValue: CustomFieldValue)

    @Insert
    fun insert(vararg customFieldValue: CustomFieldValue)

    @Update
    fun update(customFieldValue: CustomFieldValue)

    @Update
    fun update(vararg customFieldValue: CustomFieldValue)

    @Query("SELECT * from custom_field_table WHERE id LIKE :customFieldId")
    fun getField(customFieldId: String): LiveData<List<CustomField>>

    @Query("SELECT * from custom_field_table WHERE id LIKE :customFieldId")
    fun getFieldSync(customFieldId: String): List<CustomField>

    @Query("SELECT * from custom_field_table WHERE config_id LIKE :configId")
    fun getFieldsByConfig(configId: String): LiveData<List<CustomField>>

    @Query("SELECT * from custom_field_table WHERE config_id LIKE :configId")
    fun getFieldsByConfigSync(configId: String): List<CustomField>

    @Query("SELECT * from custom_field_value_table WHERE id LIKE :customFieldValueId")
    fun getFieldValue(customFieldValueId: String): LiveData<List<CustomFieldValue>>

    @Query("SELECT * from custom_field_value_table WHERE id LIKE :customFieldValueId")
    fun getFieldValueSync(customFieldValueId: String): List<CustomFieldValue>

    @Query("SELECT * from custom_field_value_table")
    fun getAllFieldValues(): LiveData<List<CustomFieldValue>>

    @Query("SELECT * from custom_field_value_table")
    fun getAllFieldValuesSync(): List<CustomFieldValue>
}