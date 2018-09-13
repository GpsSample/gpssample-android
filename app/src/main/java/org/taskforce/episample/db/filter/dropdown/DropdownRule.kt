package org.taskforce.episample.db.filter.dropdown

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.value.DropdownValue
import org.taskforce.episample.db.filter.Rule
import org.taskforce.episample.db.filter.RuleRecord


abstract class DropdownRule(forField: CustomField, val rightHandSide: DropdownValue) : Rule(forField) {
    override fun applyOperation(leftHandSide: CustomFieldValue): Boolean {
        val leftHandValue = (leftHandSide.value as DropdownValue).customDropdownId

        return operation(leftHandValue)
    }

    protected abstract fun operation(value: String): Boolean
}

class DropdownComparisonRule(private val type: DropdownRuleFactory.Rules, forField: CustomField, rightHandSide: DropdownValue) : DropdownRule(forField, rightHandSide) {
    override fun operation(value: String): Boolean {
        return type.comparator.invoke(value, rightHandSide.customDropdownId)
    }

    override fun toRecord(ruleSetId: String): RuleRecord {
        return RuleRecord(ruleSetId, forField.id, DropdownRuleFactory::class.qualifiedName!!, type.name, rightHandSide.customDropdownId)
    }
}