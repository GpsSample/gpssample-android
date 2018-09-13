package org.taskforce.episample.db.filter.doubles

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.value.DoubleValue
import org.taskforce.episample.db.filter.Rule
import org.taskforce.episample.db.filter.RuleRecord

abstract class DoubleRule(forField: CustomField, val rightHandSide: DoubleValue) : Rule(forField) {
    override fun applyOperation(leftHandSide: CustomFieldValue): Boolean {
        val leftHandValue = (leftHandSide.value as DoubleValue).doubleValue

        return operation(leftHandValue)
    }

    protected abstract fun operation(value: Double): Boolean
}

class DoubleComparisonRule(private val type: DoubleRuleFactory.Rules, forField: CustomField, rightHandSide: DoubleValue) : DoubleRule(forField, rightHandSide) {
    override fun operation(value: Double): Boolean {
        return type.comparator.invoke(value, rightHandSide.doubleValue)
    }

    override fun toRecord(ruleSetId: String): RuleRecord {
        return RuleRecord(ruleSetId, forField.id, DoubleRuleFactory::class.qualifiedName!!, type.name, rightHandSide.doubleValue.toString())
    }
}