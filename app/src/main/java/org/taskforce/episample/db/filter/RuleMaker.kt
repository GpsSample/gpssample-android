package org.taskforce.episample.db.filter

import org.taskforce.episample.db.config.customfield.CustomField


interface RuleMaker<I, T> {
    fun makeRule(ruleType: I, forField: CustomField, value: T): Rule
}