package org.taskforce.episample.collection.managers

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.item_custom_dropdown.view.*
import org.taskforce.episample.R
import org.taskforce.episample.collection.viewmodels.*
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.databinding.*
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.metadata.DateMetadata
import org.taskforce.episample.db.config.customfield.metadata.DropdownMetadata
import org.taskforce.episample.utils.inflater
import java.util.*

class CustomFieldViewFactory {

    fun generateView(rootView: View, customField: CustomField, viewModel: AbstractCustomViewModel, context: Context, parent: ViewGroup? = null) =
            when (customField.type) {
                CustomFieldType.CHECKBOX -> {
                    ItemCustomCheckboxBinding.inflate(context.inflater).apply {
                        vm = viewModel as CustomCheckboxViewModel
                    }.root
                }
                CustomFieldType.DATE -> {
                    ItemCustomDateBinding.inflate(context.inflater).apply {
                        vm = viewModel as CustomDateViewModel
                    }.root
                }
                CustomFieldType.DROPDOWN -> {
                    val binding = ItemCustomDropdownBinding.bind(rootView)
                    binding.vm = viewModel as CustomDropdownViewModel
                    binding.root
                }
                CustomFieldType.NUMBER -> {
                    val numberViewModel = viewModel as CustomNumberViewModel
                    val view = ItemCustomNumberBinding.inflate(context.inflater).apply {
                        vm = numberViewModel
                    }.root
                    
                    val editText = view.findViewById<EditText>(R.id.input)

                    val textChangeListener = object : TextWatcher {
                        var isInteger = false
                        override fun afterTextChanged(input: Editable?) {
                            input?.let {
                                if (numberViewModel.isInteger && input.contains(".")) {
                                    val noDecimal = input.toString().replace(".", "")
                                    input.clear()
                                    input.append(noDecimal)
                                }
                            }
                        }

                        override fun beforeTextChanged(input: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {}

                    }
                    textChangeListener.isInteger = numberViewModel.isInteger
                    editText.addTextChangedListener(textChangeListener)
                    
                    view
                }
                CustomFieldType.TEXT -> {
                    ItemCustomTextBinding.inflate(context.inflater).apply {
                        vm = viewModel as CustomTextViewModel
                    }.root
                }
            }

    fun generateViewModel(customField: CustomField, displaySettings: DisplaySettings, showDatePicker: (CustomField) -> Unit, context: Context? = null): AbstractCustomViewModel =
            when (customField.type) {
                CustomFieldType.TEXT -> CustomTextViewModel(customField)
                CustomFieldType.DROPDOWN -> {
                    if (context == null) {
                        throw IllegalArgumentException("CustomDropdownViewModel requires a context, " +
                                "but context argument is null.")
                    } else {
                        CustomDropdownViewModel(
                                customField,
                                context
                        ) { _, view ->
                            val index = view.dropdown.selectedItemPosition
                            when (index) {
                                0 -> null
                                else -> (customField.metadata as DropdownMetadata).items[index - 1]
                            }

                        }
                    }
                }
                CustomFieldType.DATE -> {
                    val dateVm = CustomDateViewModel(customField, displaySettings, showDatePicker)
                    if ((customField.metadata as? DateMetadata)?.useCurrentTime == true) {
                        dateVm.value.postValue(Date())
                    }
                    dateVm
                }
                CustomFieldType.CHECKBOX -> CustomCheckboxViewModel(customField)
                CustomFieldType.NUMBER -> CustomNumberViewModel(customField)
            }
}

fun CustomField.generateView(rootView: View, context: Context, vm: AbstractCustomViewModel, parent: ViewGroup? = null) =
        CustomFieldViewFactory().generateView(rootView, this, vm, context, parent)

fun CustomField.generateViewModel(displaySettings: DisplaySettings, showDatePicker: (CustomField) -> Unit, context: Context? = null) =
        CustomFieldViewFactory().generateViewModel(this, displaySettings, showDatePicker, context)
