package org.taskforce.episample.db.filter.checkbox

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.value.BooleanValue
import org.taskforce.episample.db.filter.Rule
import org.taskforce.episample.db.filter.RuleRecord


abstract class BooleanRule(forField: CustomField, val rightHandSide: BooleanValue) : Rule(forField) {
    override fun applyOperation(leftHandSide: CustomFieldValue): Boolean {
        val leftHandValue = (leftHandSide.value as BooleanValue).boolValue

        return operation(leftHandValue)
    }

    protected abstract fun operation(value: Boolean): Boolean
}

class BooleanComparisonRule(private val type: BooleanRuleFactory.Rules, forField: CustomField, rightHandSide: BooleanValue) : BooleanRule(forField, rightHandSide) {
    override fun operation(value: Boolean): Boolean {
        return type.comparator.invoke(value, rightHandSide.boolValue)
    }

    override fun toRecord(ruleSetId: String): RuleRecord {
        return RuleRecord(ruleSetId, forField.id, BooleanRuleFactory::class.qualifiedName!!, type.name, rightHandSide.boolValue.toString())
    }
}