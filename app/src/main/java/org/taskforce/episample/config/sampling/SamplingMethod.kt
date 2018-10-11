package org.taskforce.episample.config.sampling

import android.arch.persistence.room.*
import org.taskforce.episample.db.collect.ResolvedEnumeration
import org.taskforce.episample.db.config.Config
import org.taskforce.episample.db.filter.ResolvedRuleSet
import org.taskforce.episample.db.filter.RuleSet
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
        fun fromEntity(entity: ResolvedSamplingMethodEntity): SamplingMethod {
            return SamplingMethod(
                    SamplingMethodology.valueOf(entity.methodology),
                    SamplingUnits.valueOf(entity.units),
                    SamplingGrouping.valueOf(entity.grouping)
            )
        }

        fun simpleRandomSample(numberOfHouseholdsForSample: Int, totalPopulationForSampling: List<ResolvedEnumeration>): List<ResolvedEnumeration> {
            if (numberOfHouseholdsForSample > totalPopulationForSampling.size) {
                return totalPopulationForSampling.shuffled()
            }
            return totalPopulationForSampling.shuffled().subList(0, numberOfHouseholdsForSample)
        }

        fun systematicRandomSample(numberOfHouseholdsForSample: Int, totalPopulationForSampling: List<ResolvedEnumeration>): List<ResolvedEnumeration> {
            val populationForSampling = totalPopulationForSampling.sortedBy { it.dateCreated }
            if (numberOfHouseholdsForSample > populationForSampling.size) {
                return populationForSampling
            }
            val samplingFrame = populationForSampling.size.toDouble() / numberOfHouseholdsForSample.toDouble()
            var position = (Math.random() * samplingFrame) - 1
            val sample = mutableListOf(populationForSampling[Math.ceil(position).toInt()])
            while (sample.size < numberOfHouseholdsForSample) {
                position += samplingFrame
                sample.add(populationForSampling[Math.ceil(position).toInt()])
            }
            return sample
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

class ResolvedSamplingMethodEntity(
        @ColumnInfo(name = "methodology")
        var methodology: String,
        @ColumnInfo(name = "grouping")
        var grouping: String,
        @ColumnInfo(name = "units")
        var units: String,
        @ColumnInfo(name = "config_id")
        var configId: String,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()) {

    @Relation(entity = RuleSet::class, parentColumn = "id", entityColumn = "methodology_id")
    lateinit var ruleSets: List<ResolvedRuleSet>
    val unresolved: SamplingMethodEntity
        get() = SamplingMethodEntity(methodology, grouping, units, configId, id)

    fun toMethodology(): SamplingMethod = SamplingMethod.fromEntity(this)
}