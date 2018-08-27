package org.taskforce.episample.db.filter.doubles

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.value.DoubleValue
import org.taskforce.episample.db.filter.Rule

abstract class DoubleRule(forField: CustomField, val rightHandSide: DoubleValue) : Rule(forField) {
    override fun applyOperation(leftHandSide: CustomFieldValue): Boolean {
        val leftHandValue = (leftHandSide.value as DoubleValue).doubleValue

        return operation(leftHandValue)
    }

    protected abstract fun operation(value: Double): Boolean
}

class DoubleComparisonRule(private val op: (Double, Double) -> Boolean, forField: CustomField, rightHandSide: DoubleValue) : DoubleRule(forField, rightHandSide) {
    override fun operation(value: Double): Boolean {
        return op.invoke(value, rightHandSide.doubleValue)
    }
}