package org.taskforce.episample.db.config

import android.arch.persistence.room.*
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.EnumerationArea

@Entity(tableName = "enumeration_area_table",
        foreignKeys = [
            (ForeignKey(entity = Config::class, parentColumns = ["id"], childColumns = ["config_id"], onDelete = ForeignKey.CASCADE
            ))
        ])
class EnumerationArea(val name: String, 
                      @ColumnInfo(name = "config_id")
                      val configId: String,
                      @PrimaryKey
                      val id: String)

class ResolvedEnumerationArea(val name: String,
                              @ColumnInfo(name = "config_id")
                              var configId: String,
                              var id: String) {
    @Relation(parentColumn = "id", entityColumn = "enumeration_area_id")
    lateinit var points: List<EnumerationAreaPoint>
}