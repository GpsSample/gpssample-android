package org.taskforce.episample.db.collect

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.Breadcrumb
import org.taskforce.episample.db.config.Study
import org.taskforce.episample.db.converter.DateConverter
import java.util.Date
import java.util.UUID

@Entity(tableName = "gps_breadcrumb_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Study::class, parentColumns = ["id"], childColumns = ["study_id"], onDelete = ForeignKey.CASCADE
            ))
        ])

@TypeConverters(DateConverter::class)
class GpsBreadcrumb(
        override val gpsPrecision: Double,
        @ColumnInfo(name = "collector_name")
        override var collectorName: String,
        val lat: Double,
        val lng: Double,
        @ColumnInfo(name = "start_of_session")
        override val startOfSession: Boolean,
        @ColumnInfo(name = "study_id")
        var studyId: String,
        override val dateCreated: Date = Date(),
        @PrimaryKey()
        var id: String = UUID.randomUUID().toString()): Breadcrumb {


    override val location: LatLng
        get() = LatLng(lat, lng)
}
