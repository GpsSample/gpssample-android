package org.taskforce.episample.db.collect

import android.arch.persistence.room.*
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.BuiltInLandmark
import org.taskforce.episample.core.interfaces.LandmarkType
import org.taskforce.episample.core.interfaces.LandmarkTypeMetadata
import org.taskforce.episample.core.interfaces.LiveLandmark
import org.taskforce.episample.core.interfaces.LiveLandmarkType
import org.taskforce.episample.db.config.Study
import org.taskforce.episample.db.converter.BuiltInLandmarkConverter
import org.taskforce.episample.db.converter.DateConverter
import java.util.*

@Entity(tableName = "landmark_table",
        foreignKeys = [
            ForeignKey(
                    entity = Study::class, parentColumns = ["id"], childColumns = ["study_id"], onDelete = ForeignKey.CASCADE
            )
        ])

@TypeConverters(DateConverter::class, BuiltInLandmarkConverter::class)
class Landmark(
        val title: String,
        val lat: Double,
        val lng: Double,
        val note: String?,
        val image: String?,
        @ColumnInfo(name = "built_in_landmark")
        val builtInLandmark: BuiltInLandmark?,
        @ColumnInfo(name = "custom_landmark_type_id")
        val customLandmarkTypeId: String?,
        @ColumnInfo(name = "study_id")
        val studyId: String,
        @ColumnInfo(name = "gps_precision")
        val gpsPrecision: Double,
        @ColumnInfo(name = "date_created")
        val dateCreated: Date = Date(),
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()) {

    @Ignore()
    val metadata: LandmarkTypeMetadata = builtInLandmark?.let { return@let LandmarkTypeMetadata.BuiltInLandmark(it) }
    ?: customLandmarkTypeId?.let { return@let LandmarkTypeMetadata.CustomId(it) }
    ?: throw IllegalStateException()

    fun makeLiveLandmark(landmarkType: LandmarkType): LiveLandmark = LiveLandmark(title, landmarkType, note, image, LatLng(lat, lng), gpsPrecision, id, dateCreated)

}