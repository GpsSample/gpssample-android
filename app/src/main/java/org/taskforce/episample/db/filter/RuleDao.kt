package org.taskforce.episample.db.filter

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
abstract class RuleDao {
    @Insert
    abstract fun insert(set: RuleSet)

    @Insert
    abstract fun insert(record: RuleRecord)

    @Delete
    abstract fun delete(set: RuleSet)

    @Delete
    abstract fun delete(record: RuleRecord)

    @Query("SELECT * FROM rule_record_table WHERE rule_set_id like :ruleSetId")
    abstract fun getAllRulesByRuleSet(ruleSetId: String): List<ResolvedRuleRecord>
}
