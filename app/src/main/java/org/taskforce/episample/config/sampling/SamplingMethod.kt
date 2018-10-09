package org.taskforce.episample.config.sampling

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.taskforce.episample.db.config.Config
import java.io.Serializable
import java.util.*

data class SamplingMethod(var type: SamplingMethodology,
                          var units: SamplingUnits,
                          var grouping: SamplingGrouping) : Serializable {
    val id: String = UUID.randomUUID().toString()

    fun toEntity(configId: String): SamplingMethodEntity {
        return SamplingMethodEntity(type.name, grouping.name, units.name, configId, id)
    }

    companion object {
        val DEFAULT_METHOD = SamplingMethod(SamplingMethodology.SIMPLE_RANDOM_SAMPLE, SamplingUnits.PERCENT, SamplingGrouping.SUBSETS)
        fun fromEntity(entity: SamplingMethodEntity): SamplingMethod {
            return SamplingMethod(
                    SamplingMethodology.valueOf(entity.methodology),
                    SamplingUnits.valueOf(entity.units),
                    SamplingGrouping.valueOf(entity.grouping)
            )
        }
    }
}

enum class SamplingMethodology(val displayText: String) : Serializable {
    SIMPLE_RANDOM_SAMPLE("Simple Random Sampling"),
    SYSTEMATIC_RANDOM_SAMPLE("Systematic Random Sampling")
}

enum class SamplingUnits(val displayName: String) : Serializable {
    PERCENT("Percent"),
    FIXED("Households");
}

enum class SamplingGrouping {
    SUBSETS,
    STRATA,
    NONE
}

@Entity(tableName = "methodology_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Config::class, parentColumns = ["id"], childColumns = ["config_id"], onDelete = ForeignKey.CASCADE
            ))
        ])
class SamplingMethodEntity(
        @ColumnInfo(name = "methodology")
        var methodology: String,
        @ColumnInfo(name = "grouping")
        var grouping: String,
        @ColumnInfo(name = "units")
        var units: String,
        @ColumnInfo(name = "config_id")
        var configId: String,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
)