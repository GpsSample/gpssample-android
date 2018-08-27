package org.taskforce.episample.db.filter.dropdown

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.value.DropdownValue
import org.taskforce.episample.db.filter.Rule
import org.taskforce.episample.db.filter.RuleMaker


class DropdownRuleFactory {
    enum class DropdownRules(val displayName: String, val comparator: (String, String) -> Boolean) {
        IS_EQUAL_TO("=", { lhs: String, rhs: String -> lhs == rhs }),
        IS_NOT_EQUAL_TO("≠", { lhs: String, rhs: String -> lhs != rhs });
    }

    companion object: RuleMaker<DropdownRules, String> {
        override fun makeRule(ruleType: DropdownRules, forField: CustomField, value: String): Rule {
            return DropdownComparisonRule(ruleType.comparator, forField, DropdownValue(value))
        }
    }
}