package org.taskforce.episample.db.collect

import android.arch.persistence.room.*
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.Breadcrumb
import org.taskforce.episample.db.config.Study
import org.taskforce.episample.db.converter.DateConverter
import java.util.*

@Entity(tableName = "gps_breadcrumb_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Study::class, parentColumns = ["id"], childColumns = ["study_id"]
            ))
        ])

@TypeConverters(DateConverter::class)
class GpsBreadcrumb(
        override val dateCreated: Date,
        override val gpsPrecision: Double,
        @ColumnInfo(name = "collector_name")
        var collectorName: String,
        val lat: Double,
        val lng: Double,
        @ColumnInfo(name = "study_id")
        var studyId: String): Breadcrumb {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    override val location: LatLng
        get() = LatLng(lat, lng)
}
