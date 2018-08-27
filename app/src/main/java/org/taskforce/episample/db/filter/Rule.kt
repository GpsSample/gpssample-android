package org.taskforce.episample.db.filter

import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue

abstract class Rule(val forField: CustomField) {
    abstract fun applyOperation(leftHandSide: CustomFieldValue): Boolean
}