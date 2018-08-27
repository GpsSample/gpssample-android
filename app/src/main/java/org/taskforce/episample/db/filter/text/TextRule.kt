package org.taskforce.episample.db.filter.text

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.value.TextValue
import org.taskforce.episample.db.filter.Rule


abstract class TextRule(forField: CustomField, val rightHandSide: TextValue) : Rule(forField) {
    override fun applyOperation(leftHandSide: CustomFieldValue): Boolean {
        val leftHandValue = (leftHandSide.value as TextValue).text

        return operation(leftHandValue)
    }

    protected abstract fun operation(value: String): Boolean
}

class TextComparisonRule(private val op: (String, String) -> Boolean, forField: CustomField, rightHandSide: TextValue) : TextRule(forField, rightHandSide) {
    override fun operation(value: String): Boolean {
        return op.invoke(value, rightHandSide.text)
    }
}