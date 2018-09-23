package org.taskforce.episample.db.sampling.subsets

import android.arch.persistence.room.*
import org.taskforce.episample.db.config.Config
import org.taskforce.episample.db.filter.RuleSet
import java.util.*

@Entity(tableName = "subset_table",
        foreignKeys = [
            (ForeignKey(
                    entity = Config::class, parentColumns = ["id"], childColumns = ["config_id"], onDelete = ForeignKey.CASCADE
            )),
            (ForeignKey(
                    entity = RuleSet::class, parentColumns = ["id"], childColumns = ["rule_set_id"], onDelete = ForeignKey.CASCADE
            ))
        ])
class Subset(
        @ColumnInfo(name = "config_id")
        var configId: String,
        @ColumnInfo(name = "rule_set_id")
        var subset: String,
        @PrimaryKey
        var id: String = UUID.randomUUID().toString()
)

class ResolvedSubset(
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