package org.taskforce.episample.db.collect

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.converter.DateConverter
import java.util.*

@TypeConverters(DateConverter::class)
class ResolvedEnumeration(
        val id: String,
        val lat: Double,
        val lng: Double,
        val note: String?,
        @ColumnInfo(name = "is_complete")
        val isIncomplete: Boolean,
        @ColumnInfo(name = "gps_precision")
        val gpsPrecision: Double,
        val title: String? = null,
        val image: String? = null,
        @ColumnInfo(name = "date_created")
        val dateCreated: Date = Date()
) {

    @Relation(parentColumn = "id", entityColumn = "enumeration_id")
    lateinit var customFieldValues: List<CustomFieldValue>
}

@Dao
interface ResolvedEnumerationDao {
    @Query("SELECT * from enumeration_table WHERE id LIKE :enumerationId")// e INNER JOIN custom_field_value_table c ON c.enumeration_id = e.id WHERE e.id LIKE :enumerationId")
    fun getEnumeration(enumerationId: String): LiveData<List<ResolvedEnumeration>>

    @Query("SELECT * from enumeration_table WHERE id LIKE :enumerationId")// e INNER JOIN custom_field_value_table c ON c.enumeration_id = e.id WHERE e.id LIKE :enumerationId")
    fun getEnumerationSync(enumerationId: String): List<ResolvedEnumeration>
}