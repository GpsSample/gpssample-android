package org.taskforce.episample.db.collect

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.core.interfaces.LiveEnumeration
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.converter.DateConverter
import org.taskforce.episample.db.sampling.SampleEnumerationEntity
import java.util.*

@TypeConverters(DateConverter::class)
class ResolvedEnumeration(
        val id: String,
        val lat: Double,
        val lng: Double,
        val note: String?,
        @ColumnInfo(name = "is_excluded")
        val isExcluded: Boolean,
        @ColumnInfo(name = "is_complete")
        val isIncomplete: Boolean,
        @ColumnInfo(name = "gps_precision")
        val gpsPrecision: Double,
        @ColumnInfo(name = "collector_name")
        val collectorName: String,
        val title: String? = null,
        val image: String? = null,
        @ColumnInfo(name = "incomplete_reason")
        val incompleteReason: String? = null,
        @ColumnInfo(name = "date_created")
        val dateCreated: Date = Date()
) {

    @Relation(parentColumn = "id", entityColumn = "enumeration_id")
    lateinit var customFieldValues: List<CustomFieldValue>

    fun makeLiveEnumeration(): Enumeration = LiveEnumeration(collectorName, image,
            isIncomplete, isExcluded, title, note, LatLng(lat, lng), gpsPrecision, "TODO", customFieldValues.map { it as org.taskforce.episample.core.interfaces.CustomFieldValue }, id, incompleteReason, dateCreated)

    fun toSampleEnumerationEntity(sampleId: String): SampleEnumerationEntity = SampleEnumerationEntity(sampleId, id)
}

@Dao
interface ResolvedEnumerationDao {
    @Query("SELECT * from enumeration_table WHERE id LIKE :enumerationId AND is_deleted= 0")// e INNER JOIN custom_field_value_table c ON c.enumeration_id = e.id WHERE e.id LIKE :enumerationId")
    fun getResolvedEnumeration(enumerationId: String): LiveData<ResolvedEnumeration?>

    @Query("SELECT * from enumeration_table WHERE id LIKE :enumerationId AND is_deleted= 0")// e INNER JOIN custom_field_value_table c ON c.enumeration_id = e.id WHERE e.id LIKE :enumerationId")
    fun getResolvedEnumerationSync(enumerationId: String): ResolvedEnumeration?

    @Query("SELECT * from enumeration_table WHERE study_id LIKE :studyId AND is_deleted= 0")
    fun getResolvedEnumerations(studyId: String): LiveData<ResolvedEnumeration?>

    @Query("SELECT * from enumeration_table WHERE study_id LIKE :studyId AND is_deleted= 0")
    fun getResolvedEnumerationsSync(studyId: String): List<ResolvedEnumeration>
}