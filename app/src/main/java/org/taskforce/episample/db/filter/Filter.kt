package org.taskforce.episample.db.filter

import org.taskforce.episample.db.collect.ResolvedEnumeration
import org.taskforce.episample.db.config.customfield.CustomFieldValue


class Filter(private val rules: List<Rule>) {
    fun filterAny(enumerations: List<ResolvedEnumeration>): List<ResolvedEnumeration> {
        return enumerations.filter { enumeration ->
            enumeration.customFieldValues.map {
                Pair(it, ruleFor(it))
            }.map {
                it.second?.applyOperation(it.first)
            }.any {
                it == true
            }
        }
    }

    fun filterAll(enumerations: List<ResolvedEnumeration>): List<ResolvedEnumeration> {
        return enumerations.filter { enumeration ->
            enumeration.customFieldValues.map {
                Pair(it, ruleFor(it))
            }.map {
                it.second?.applyOperation(it.first)
            }.all {
                it == true
            }
        }
    }

    private fun ruleFor(customFieldValue: CustomFieldValue): Rule? {
        return rules.find {
            it.forField.id == customFieldValue.customFieldId
        }
    }
}
