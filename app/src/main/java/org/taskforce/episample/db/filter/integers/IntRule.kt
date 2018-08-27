package org.taskforce.episample.db.filter.integers

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.value.IntValue
import org.taskforce.episample.db.filter.Rule

abstract class IntRule(forField: CustomField, val rightHandSide: IntValue) : Rule(forField) {
    override fun applyOperation(leftHandSide: CustomFieldValue): Boolean {
        val leftHandValue = (leftHandSide.value as IntValue).intValue

        return operation(leftHandValue)
    }

    protected abstract fun operation(value: Int): Boolean
}

class IntComparisonRule(private val op: (Int, Int) -> Boolean, forField: CustomField, rightHandSide: IntValue) : IntRule(forField, rightHandSide) {
    override fun operation(value: Int): Boolean {
        return op.invoke(value, rightHandSide.intValue)
    }
}