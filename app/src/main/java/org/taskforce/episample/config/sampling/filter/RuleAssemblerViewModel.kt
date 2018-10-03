package org.taskforce.episample.config.sampling.filter

import android.arch.lifecycle.MutableLiveData
import android.databinding.BaseObservable
import android.view.View
import org.taskforce.episample.db.filter.ResolvedRuleRecord
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.checkbox.BooleanRuleFactory
import org.taskforce.episample.db.filter.doubles.DoubleRuleFactory
import org.taskforce.episample.db.filter.dropdown.DropdownRuleFactory
import org.taskforce.episample.db.filter.integers.IntRuleFactory
import org.taskforce.episample.db.filter.text.TextRuleFactory


class RuleAssemblerViewModel(private val ruleSetId: String, val fields: List<CustomFieldForRules>) : BaseObservable() {
    var position: Int = 0
        set(value) {
            field = value
            notifyChange()
        }
    val ruleNumber: String
        get() = "${position + 1}"
    var selectedField: CustomFieldForRules? = null
        set(value) {
            field = value
            notifyChange()
        }
    val events = MutableLiveData<Event>()

    private val isFieldSelected
        get() = selectedField != null

    val operatorAndValueVisibility: Int
        get() = if (isFieldSelected) View.VISIBLE else View.GONE

    val valueSelectVisibility: Int
        get() = when (selectedField) {
            is CustomFieldForRules.DropdownField,
            is CustomFieldForRules.BooleanField -> View.VISIBLE
            else -> View.GONE
        }

    val valueEntryVisibility: Int
        get() = when (selectedField) {
            is CustomFieldForRules.DropdownField,
            is CustomFieldForRules.BooleanField -> View.GONE
            else -> View.VISIBLE
        }

    /*
        The following are used to restore state
     */
    var selectedFieldIndex: Int = 0 //by default the first option is selected
    var selectedRuleIndex: Int = 0 //by default the first option is selected
    var value: String = "" // either there will be a string value or a selected index for the value (they are dropdowns or text entry fields
    var selectedValueIndex: Int? = null
    val isValid: Boolean
        get() {
            return when (selectedField) {
                is CustomFieldForRules.DropdownField,
                is CustomFieldForRules.BooleanField -> true
                is CustomFieldForRules.DateField,
                is CustomFieldForRules.TextField -> {
                    value.isNotBlank()
                }
                is CustomFieldForRules.IntegerField -> {
                    try {
                        value.toInt()
                        true
                    } catch (throwable: Throwable) {
                        false
                    }
                }
                is CustomFieldForRules.DoubleField -> {
                    try {
                        value.toDouble()
                        true
                    } catch (throwable: Throwable) {
                        false
                    }
                }
                null -> throw IllegalAccessError("Cannot convert to record without a selected field -- this should be unpossible")
            }
        }

    fun deletePressed(view: View) {
        events.value = Event.DeleteEvent(position)
    }

    fun toRuleRecord(): RuleRecord {
        @Suppress("SpellCheckingInspection")
        return when (selectedField) {
            is CustomFieldForRules.DropdownField -> {
                val ruleName = DropdownRuleFactory.Rules.values()[selectedRuleIndex].name
                RuleRecord(ruleSetId, selectedField!!.fieldId, ResolvedRuleRecord.Factories.DROPDOWN_FACTORY.factoryName, ruleName, (selectedField as CustomFieldForRules.DropdownField).values[selectedValueIndex!!].id)
            }
            is CustomFieldForRules.TextField -> {
                val ruleName = TextRuleFactory.Rules.values()[selectedRuleIndex].name
                RuleRecord(ruleSetId, selectedField!!.fieldId, ResolvedRuleRecord.Factories.TEXT_FACTORY.factoryName, ruleName, value)
            }
            is CustomFieldForRules.BooleanField -> {
                val ruleName = BooleanRuleFactory.Rules.values()[selectedRuleIndex].name
                RuleRecord(ruleSetId, selectedField!!.fieldId, ResolvedRuleRecord.Factories.BOOLEAN_FACTORY.factoryName, ruleName, (selectedFieldIndex == 0).toString())
            }
            is CustomFieldForRules.IntegerField -> {
                val ruleName = IntRuleFactory.Rules.values()[selectedRuleIndex].name
                RuleRecord(ruleSetId, selectedField!!.fieldId, ResolvedRuleRecord.Factories.INT_FACTORY.factoryName, ruleName, value)
            }
            is CustomFieldForRules.DoubleField -> {
                val ruleName = DoubleRuleFactory.Rules.values()[selectedRuleIndex].name
                RuleRecord(ruleSetId, selectedField!!.fieldId, ResolvedRuleRecord.Factories.DOUBLE_FACTORY.factoryName, ruleName, value)
            }
            is CustomFieldForRules.DateField -> {
                val ruleName = DoubleRuleFactory.Rules.values()[selectedRuleIndex].name
                RuleRecord(ruleSetId, selectedField!!.fieldId, ResolvedRuleRecord.Factories.DATE_FACTORY.factoryName, ruleName, value)
            }
            null -> throw IllegalAccessError("Cannot convert to record without a selected field -- this should be unpossible")
        }
    }

    companion object {
        fun from(ruleRecord: RuleRecord, fields: List<CustomFieldForRules>): RuleAssemblerViewModel {
            return RuleAssemblerViewModel(ruleRecord.ruleSetId, fields).apply {
                selectedField = fields.find {
                    it.fieldId == ruleRecord.customFieldId
                }
            }
        }
    }

    sealed class Event {
        class DeleteEvent(val position: Int) : Event()
    }
}

