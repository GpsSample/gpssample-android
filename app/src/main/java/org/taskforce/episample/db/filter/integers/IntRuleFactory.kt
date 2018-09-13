package org.taskforce.episample.db.filter.integers

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.value.IntValue
import org.taskforce.episample.db.filter.Rule
import org.taskforce.episample.db.filter.RuleMaker

class IntRuleFactory {
    enum class Rules(val displayName: String, val comparator: (Int, Int) -> Boolean) {
        LESS_THAN("<", { lhs: Int, rhs: Int -> lhs < rhs }),
        LESS_THAN_OR_EQUAL_TO("≤", { lhs: Int, rhs: Int -> lhs <= rhs }),
        GREATER_THAN(">", { lhs: Int, rhs: Int -> lhs > rhs }),
        GREATER_THAN_OR_EQUAL_TO("≥", { lhs: Int, rhs: Int -> lhs >= rhs }),
        IS_EQUAL_TO("=", { lhs: Int, rhs: Int -> lhs == rhs }),
        IS_NOT_EQUAL_TO("≠", { lhs: Int, rhs: Int -> lhs != rhs });
    }

    companion object: RuleMaker<Rules, Int> {
        override fun makeRule(ruleType: Rules, forField: CustomField, value: Int): Rule {
            return IntComparisonRule(ruleType, forField, IntValue(value))
        }
    }
}