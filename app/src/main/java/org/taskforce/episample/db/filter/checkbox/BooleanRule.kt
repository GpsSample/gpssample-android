package org.taskforce.episample.db.filter.checkbox

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.value.BooleanValue
import org.taskforce.episample.db.filter.Rule


abstract class BooleanRule(forField: CustomField, val rightHandSide: BooleanValue) : Rule(forField) {
    override fun applyOperation(leftHandSide: CustomFieldValue): Boolean {
        val leftHandValue = (leftHandSide.value as BooleanValue).boolValue

        return operation(leftHandValue)
    }

    protected abstract fun operation(value: Boolean): Boolean
}

class BooleanComparisonRule(private val op: (Boolean, Boolean) -> Boolean, forField: CustomField, rightHandSide: BooleanValue) : BooleanRule(forField, rightHandSide) {
    override fun operation(value: Boolean): Boolean {
        return op.invoke(value, rightHandSide.boolValue)
    }
}