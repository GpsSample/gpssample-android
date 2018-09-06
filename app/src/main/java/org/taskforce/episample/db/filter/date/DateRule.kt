package org.taskforce.episample.db.filter.date

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.value.DateValue
import org.taskforce.episample.db.filter.Rule
import java.util.*


abstract class DateRule(forField: CustomField, val rightHandSide: DateValue) : Rule(forField) {
    override fun applyOperation(leftHandSide: CustomFieldValue): Boolean {
        val leftHandValue = (leftHandSide.value as DateValue).dateValue

        return operation(leftHandValue)
    }

    protected abstract fun operation(value: Date): Boolean
}

class DateComparisonRule(private val op: (Date, Date) -> Boolean, forField: CustomField, rightHandSide: DateValue) : DateRule(forField, rightHandSide) {
    override fun operation(value: Date): Boolean {
        return op.invoke(value, rightHandSide.dateValue)
    }
}