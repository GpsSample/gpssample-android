package org.taskforce.episample.db.filter.text

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.value.TextValue
import org.taskforce.episample.db.filter.Rule
import org.taskforce.episample.db.filter.RuleRecord


abstract class TextRule(forField: CustomField, val rightHandSide: TextValue) : Rule(forField) {
    override fun applyOperation(leftHandSide: CustomFieldValue): Boolean {
        val leftHandValue = (leftHandSide.value as TextValue).text

        return operation(leftHandValue)
    }

    protected abstract fun operation(value: String): Boolean
}

class TextComparisonRule(private val type: TextRuleFactory.Rules, forField: CustomField, rightHandSide: TextValue) : TextRule(forField, rightHandSide) {
    override fun operation(value: String): Boolean {
        return type.comparator.invoke(value, rightHandSide.text)
    }

    override fun toRecord(ruleSetId: String): RuleRecord {
        return RuleRecord(ruleSetId, forField.id, TextRuleFactory::class.qualifiedName!!, type.name, rightHandSide.text)
    }
}