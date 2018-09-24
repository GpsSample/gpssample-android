package org.taskforce.episample.config.sampling.filter

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_rule_set_creation.*
import org.taskforce.episample.R
import org.taskforce.episample.databinding.ActivityRuleSetCreationBinding
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import java.util.*


class RuleSetCreationActivity : FragmentActivity() {

    private lateinit var viewModel: RuleSetCreationViewModel
    private lateinit var ruleSetId: String
    private var ruleSet: RuleSet? = null
    private var rules: List<RuleRecord>? = null
    private lateinit var fields: List<CustomFieldForRules>
    private var adapter: RulesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("UNCHECKED_CAST")
        fields = intent.extras.getParcelableArray(EXTRA_CUSTOM_FIELDS).toList() as List<CustomFieldForRules>

        val binding = DataBindingUtil.setContentView<ActivityRuleSetCreationBinding>(this, R.layout.activity_rule_set_creation)
        binding.setLifecycleOwner(this)

        if (!intent.hasExtra(EXTRA_RULESET)) {
            ruleSetId = intent.extras.getString(EXTRA_RULESET_ID) ?: UUID.randomUUID().toString()
        } else {
            ruleSet = intent.extras.getParcelable(EXTRA_RULESET)
            ruleSetId = ruleSet!!.id
            @Suppress("UNCHECKED_CAST")
            rules = intent.getParcelableArrayExtra(EXTRA_RULES).toList() as List<RuleRecord>
        }
        adapter = RulesAdapter(ruleSetId, fields, this)
        ruleRecyclerView.adapter = adapter
        ruleRecyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProviders.of(this, RuleSetCreationViewModelFactory()).get(RuleSetCreationViewModel::class.java)
        ruleSet?.let {
            viewModel.isAnyChecked.set(it.isAny)
            viewModel.name.postValue(it.name)
            adapter?.rules?.clear()
            adapter?.addAll(rules!!)
        }
        binding.vm = viewModel
        binding.vm?.closeEvents?.observe(this, Observer<RuleSetCreationViewModel.Event> { t ->
            t?.let {
                when (it) {
                    is RuleSetCreationViewModel.Event.CloseEvent -> {
                        hideKeyboard()
                        finish()
                    }
                    is RuleSetCreationViewModel.Event.SaveEvent -> {
                        hideKeyboard()
                        setResult(Activity.RESULT_OK, intentWithRuleSet())
                        finish()
                    }
                    is RuleSetCreationViewModel.Event.AddRuleEvent -> adapter?.addRule()
                    else -> {
                        //nop
                    }
                }
            }
        })
    }

    private fun intentWithRuleSet(): Intent {
        val returnIntent = Intent()
        returnIntent.putExtra(EXTRA_RESULT_RULESET, generateRuleSet())
        returnIntent.putExtra(EXTRA_RESULT_RULES, generateRules().toTypedArray())
        return returnIntent
    }

    private fun generateRuleSet(): RuleSet {
        return RuleSet(viewModel.name.value!!, viewModel.isAnyChecked.get(), ruleSetId)
    }

    private fun generateRules(): List<RuleRecord> {
        return adapter!!.extractRulesFromData()
    }

    private fun hideKeyboard() {
        // Check if no view has focus:
        if (currentFocus != null) {
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    companion object {
        fun startActivity(fragment: Fragment, customFields: List<CustomField>, configId: String) {
            val intent = Intent(fragment.context, RuleSetCreationActivity::class.java)
            val fields: List<CustomFieldForRules> = customFields.map { field ->
                when (field.type) {
                    CustomFieldType.TEXT,
                    CustomFieldType.DATE -> {
                        //TODO handle the dates
                        CustomFieldForRules.TextField(field.id, field.name, field.type)
                    }
                    CustomFieldType.NUMBER -> {
                        if ((field.metadata as org.taskforce.episample.db.config.customfield.metadata.NumberMetadata).isIntegerOnly) {
                            CustomFieldForRules.IntegerField(field.id, field.name, field.type)
                        } else {
                            CustomFieldForRules.DoubleField(field.id, field.name, field.type)
                        }
                    }
                    CustomFieldType.CHECKBOX -> {
                        CustomFieldForRules.BooleanField(field.id, field.name, field.type)
                    }
                    CustomFieldType.DROPDOWN -> {
                        val dest = field.metadata as org.taskforce.episample.db.config.customfield.metadata.DropdownMetadata
                        val values = dest.items.map {
                            CustomFieldForRules.DropdownField.Value(it.key, it.value
                                    ?: "NO VALUE")
                        }
                        CustomFieldForRules.DropdownField(field.id, field.name, field.type, values)
                    }
                }
            }
            intent.putExtra(EXTRA_CUSTOM_FIELDS, fields.toTypedArray())
            intent.putExtra(EXTRA_CONFIG_ID, configId)
            fragment.startActivityForResult(intent, REQUEST_CODE_FOR_RULESET)
        }

        fun startActivity(fragment: Fragment, customFields: List<CustomField>, configId: String, ruleSet: RuleSet, rules: List<RuleRecord>) {
            val intent = Intent(fragment.context, RuleSetCreationActivity::class.java)
            val fields: List<CustomFieldForRules> = customFields.map { field ->
                when (field.type) {
                    CustomFieldType.TEXT,
                    CustomFieldType.DATE -> {
                        //TODO handle the dates
                        CustomFieldForRules.TextField(field.id, field.name, field.type)
                    }
                    CustomFieldType.NUMBER -> {
                        if ((field.metadata as org.taskforce.episample.db.config.customfield.metadata.NumberMetadata).isIntegerOnly) {
                            CustomFieldForRules.IntegerField(field.id, field.name, field.type)
                        } else {
                            CustomFieldForRules.DoubleField(field.id, field.name, field.type)
                        }
                    }
                    CustomFieldType.CHECKBOX -> {
                        CustomFieldForRules.BooleanField(field.id, field.name, field.type)
                    }
                    CustomFieldType.DROPDOWN -> {
                        val dest = field.metadata as org.taskforce.episample.db.config.customfield.metadata.DropdownMetadata
                        val values = dest.items.map {
                            CustomFieldForRules.DropdownField.Value(it.key, it.value
                                    ?: "NO VALUE")
                        }
                        CustomFieldForRules.DropdownField(field.id, field.name, field.type, values)
                    }
                }
            }
            intent.putExtra(EXTRA_CUSTOM_FIELDS, fields.toTypedArray())
            intent.putExtra(EXTRA_CONFIG_ID, configId)
            intent.putExtra(EXTRA_RULESET, ruleSet)
            intent.putExtra(EXTRA_RULES, rules.toTypedArray())
            fragment.startActivityForResult(intent, REQUEST_CODE_FOR_RULESET)
        }

        const val REQUEST_CODE_FOR_RULESET = 139
        const val EXTRA_RESULT_RULESET = "EXTRA_RESULT_RULE_SET"
        const val EXTRA_RESULT_RULES = "EXTRA_RESULT_RULES"
        const val EXTRA_CUSTOM_FIELDS = "EXTRA_CUSTOM_FIELDS"
        const val EXTRA_RULESET_ID = "EXTRA_RULESET_ID"
        const val EXTRA_CONFIG_ID = "EXTRA_CONFIG_ID"
        const val EXTRA_RULES = "EXTRA_RULES"
        const val EXTRA_RULESET = "EXTRA_RULESET"
    }
}