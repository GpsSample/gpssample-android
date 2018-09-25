package org.taskforce.episample.config.sampling.filter

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.databinding.ItemRuleAssemblerBinding
import org.taskforce.episample.db.config.customfield.CustomDateType
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.checkbox.BooleanRuleFactory
import org.taskforce.episample.db.filter.date.DateRuleFactory
import org.taskforce.episample.db.filter.dropdown.DropdownRuleFactory
import org.taskforce.episample.db.filter.integers.IntRuleFactory
import org.taskforce.episample.db.filter.text.TextRuleFactory
import java.util.*


class RulesAdapter(val ruleSetId: String, val fields: List<CustomFieldForRules>, val lifecycleOwner: LifecycleOwner, val dateListener: DateClickedListener, val displaySettings: DisplaySettings) : RecyclerView.Adapter<RulesAdapter.RuleHolder>(), Observer<RuleAssemblerViewModel.Event> {
    val rules = mutableListOf<RuleAssemblerViewModel>().apply {
        add(RuleAssemblerViewModel(ruleSetId, fields))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleHolder {
        val binding = DataBindingUtil.inflate<ItemRuleAssemblerBinding>(LayoutInflater.from(parent.context), R.layout.item_rule_assembler, parent, false)
        return RuleHolder(binding, fields, lifecycleOwner, this, dateListener)
    }

    override fun getItemCount(): Int {
        return rules.size
    }

    override fun onBindViewHolder(holder: RuleHolder, position: Int) {
        holder.bind(rules[position], position)
    }

    fun addRule() {
        rules.add(RuleAssemblerViewModel(ruleSetId, fields))
        notifyDataSetChanged()
    }

    override fun onChanged(t: RuleAssemblerViewModel.Event?) {
        t?.let {
            when (it) {
                is RuleAssemblerViewModel.Event.DeleteEvent -> {
                    rules.removeAt(it.position)
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun extractRulesFromData(): List<RuleRecord> {
        return rules.map {
            it.toRuleRecord()
        }
    }

    fun addAll(newRules: List<RuleRecord>) {
        newRules.forEach {
            this.rules.add(
                    RuleAssemblerViewModel.from(it, fields)
            )
        }
    }

    fun updateDateField(customFieldId: String, time: Date) {
        val viewModel = rules.find {
            it.selectedField?.fieldId == customFieldId
        }

        val customFieldForRules = viewModel?.selectedField as CustomFieldForRules.DateField
        viewModel.value = when (customFieldForRules.metaData.dateType) {
            CustomDateType.DATE -> displaySettings.getFormattedDate(time, false)
            CustomDateType.TIME -> displaySettings.getFormattedTime(time)
            CustomDateType.DATE_TIME -> displaySettings.getFormattedDateWithTime(time, false)
        }
        viewModel.notifyChange()
    }

    class RuleHolder(val binding: ItemRuleAssemblerBinding, val fields: List<CustomFieldForRules>, val lifecycleOwner: LifecycleOwner, private val deleteObserver: Observer<RuleAssemblerViewModel.Event>, val dateListener: DateClickedListener) : RecyclerView.ViewHolder(binding.root) {
        private val fieldSpinnerOnItemSelectListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nop
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.vm?.selectedFieldIndex = position
                binding.vm?.selectedField = fields[position]
                binding.vm?.selectedRuleIndex = 0
                binding.vm?.let { setState(it) }
            }

        }
        private val operatorSpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nop
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.vm?.selectedRuleIndex = position
            }

        }
        private val valueSpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nop
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.vm?.selectedValueIndex = position
            }
        }

        private val dateClickedListener = View.OnClickListener { dateListener.onDateClicked(binding.vm?.selectedField as CustomFieldForRules.DateField) }

        fun bind(viewModel: RuleAssemblerViewModel, position: Int) {
            viewModel.position = position
            binding.vm = viewModel
            if (viewModel.selectedField == null) {
                binding.vm?.selectedField = fields[viewModel.selectedRuleIndex]
            }
            setState(viewModel)
        }

        private fun addListeners() {
            binding.ruleSpinner.onItemSelectedListener = fieldSpinnerOnItemSelectListener
            binding.ruleValueSpinner.onItemSelectedListener = valueSpinnerOnItemSelectedListener
            binding.ruleOperatorSpinner.onItemSelectedListener = operatorSpinnerOnItemSelectedListener
        }

        private fun removeListeners() {
            binding.ruleSpinner.onItemSelectedListener = null
            binding.ruleValueSpinner.onItemSelectedListener = null
            binding.ruleOperatorSpinner.onItemSelectedListener = null
        }

        private fun setState(viewModel: RuleAssemblerViewModel) {
            removeListeners()
            binding.vm?.events?.observe(lifecycleOwner, deleteObserver)
            binding.ruleSpinner.adapter = ArrayAdapter<String>(binding.root.context, android.R.layout.simple_spinner_dropdown_item, fields.map { it.fieldName })

            binding.ruleSpinner.setSelection(viewModel.selectedFieldIndex, false)

            when (viewModel.selectedField) {
                is CustomFieldForRules.TextField -> {
                    val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_dropdown_item, TextRuleFactory.Rules.values().map { it.displayName })
                    binding.ruleOperatorSpinner.adapter = adapter
                }
                is CustomFieldForRules.IntegerField -> {
                    val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_dropdown_item, IntRuleFactory.Rules.values().map { it.displayName })
                    binding.ruleOperatorSpinner.adapter = adapter
                }
                is CustomFieldForRules.DateField -> {
                    val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_dropdown_item, DateRuleFactory.Rules.values().map { it.displayName })
                    binding.ruleOperatorSpinner.adapter = adapter
                    binding.ruleValueEditText.setOnClickListener(dateClickedListener)
                }
                is CustomFieldForRules.BooleanField -> {
                    val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_dropdown_item, BooleanRuleFactory.Rules.values().map { it.displayName })
                    binding.ruleOperatorSpinner.adapter = adapter
                    val options = (viewModel.selectedField as CustomFieldForRules.BooleanField).values
                    binding.ruleValueSpinner.adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_dropdown_item, options)
                }
                is CustomFieldForRules.DropdownField -> {
                    val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_dropdown_item, DropdownRuleFactory.Rules.values().map { it.displayName })
                    val options = (viewModel.selectedField as CustomFieldForRules.DropdownField).values.map { it.name }
                    binding.ruleOperatorSpinner.adapter = adapter
                    binding.ruleValueSpinner.adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_dropdown_item, options)
                }
            }

            binding.ruleOperatorSpinner.setSelection(viewModel.selectedRuleIndex, false)
            if (viewModel.selectedValueIndex != null) {
                binding.ruleValueSpinner.setSelection(viewModel.selectedValueIndex!!, false)
            }
            addListeners()
        }
    }
}

interface DateClickedListener {
    fun onDateClicked(field: CustomFieldForRules.DateField)
}