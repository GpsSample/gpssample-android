package org.taskforce.episample.db.filter.text

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.value.TextValue
import org.taskforce.episample.db.filter.Rule
import org.taskforce.episample.db.filter.RuleMaker


class TextRuleFactory {
    enum class TextRules(val displayName: String, val comparator: (String, String) -> Boolean) {
        IS_EQUAL_TO("=", { lhs: String, rhs: String -> lhs == rhs }),
        IS_NOT_EQUAL_TO("≠", { lhs: String, rhs: String -> lhs != rhs });
    }

    companion object: RuleMaker<TextRules, String> {
        override fun makeRule(ruleType: TextRules, forField: CustomField, value: String): Rule {
            return TextComparisonRule(ruleType.comparator, forField, TextValue(value))
        }
    }
}