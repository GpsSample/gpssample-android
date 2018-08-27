package org.taskforce.episample.db.filter.doubles

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.value.DoubleValue
import org.taskforce.episample.db.filter.Rule
import org.taskforce.episample.db.filter.RuleMaker


class DoubleRuleFactory {
    enum class DoubleRules(val displayName: String, val comparator: (Double, Double) -> Boolean) {
        LESS_THAN("<", { lhs: Double, rhs: Double -> lhs < rhs }),
        LESS_THAN_OR_EQUAL_TO("≤", { lhs: Double, rhs: Double -> lhs <= rhs }),
        GREATER_THAN(">", { lhs: Double, rhs: Double -> lhs > rhs }),
        GREATER_THAN_OR_EQUAL_TO("≥", { lhs: Double, rhs: Double -> lhs >= rhs }),
        IS_EQUAL_TO("=", { lhs: Double, rhs: Double -> lhs == rhs }),
        IS_NOT_EQUAL_TO("≠", { lhs: Double, rhs: Double -> lhs != rhs });
    }

    companion object: RuleMaker<DoubleRules, Double> {
        override fun makeRule(ruleType: DoubleRules, forField: CustomField, value: Double): Rule {
            return DoubleComparisonRule(ruleType.comparator, forField, DoubleValue(value))
        }
    }
}