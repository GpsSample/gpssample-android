package org.taskforce.episample.db.sampling.strata

import android.arch.persistence.room.*
import org.taskforce.episample.db.config.Config
import org.taskforce.episample.db.filter.RuleSet
import java.io.Serializable
import java.util.*


@Entity(tableName = "strata_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Config::class, parentColumns = ["id"], childColumns = ["config_id"], onDelete = ForeignKey.CASCADE
            )),
            (ForeignKey(
                    entity = RuleSet::class, parentColumns = ["id"], childColumns = ["rule_set_id"], onDelete = ForeignKey.CASCADE
            ))
        ])
class Strata(
        @ColumnInfo(name = "config_id")
        var configId: String,
        @ColumnInfo(name = "rule_set_id")
        var ruleSetId: String,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
): Serializable

class ResolvedStrata(
        @ColumnInfo(name = "config_id")
        var configId: String,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
) {
    @Relation(parentColumn = "rule_set_id", entityColumn = "id")
    lateinit var ruleSets: List<RuleSet>

    val ruleSet: RuleSet
        get() = ruleSets.first()
}