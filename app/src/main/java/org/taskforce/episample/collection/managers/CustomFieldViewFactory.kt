package org.taskforce.episample.collection.managers

import android.content.Context
import android.view.ViewGroup
import io.reactivex.Single
import kotlinx.android.synthetic.main.item_custom_dropdown.view.*
import org.taskforce.episample.collection.viewmodels.*
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.databinding.*
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.utils.inflater
import java.util.*

class CustomFieldViewFactory {

    fun generateView(customField: CustomField, viewModel: AbstractCustomViewModel, context: Context, parent: ViewGroup? = null) =
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
                    ItemCustomDropdownBinding.inflate(context.inflater).apply {
                        vm = viewModel as CustomDropdownViewModel
                    }.root
                }
                CustomFieldType.NUMBER -> {
                    ItemCustomNumberBinding.inflate(context.inflater).apply {
                        vm = viewModel as CustomNumberViewModel
                    }.root
                }
                CustomFieldType.TEXT -> {
                    ItemCustomTextBinding.inflate(context.inflater).apply {
                        vm = viewModel as CustomTextViewModel
                    }.root
                }
            }

    fun generateViewModel(customField: CustomField, context: Context? = null): AbstractCustomViewModel =
            when (customField.type) {
                CustomFieldType.TEXT -> CustomTextViewModel(customField)
                CustomFieldType.DROPDOWN -> {
                    if (context == null) {
                            throw IllegalArgumentException("CustomDropdownViewModel requires a context, " +
                                    "but context argument is null.")
                        }
                        else {
                            CustomDropdownViewModel(
                                    customField,
                                    context,
                                    { adapter, view ->
                                        adapter.getItem(view.dropdown.selectedItemPosition)
                                    }
                            )
                        }
                    }
                CustomFieldType.DATE -> CustomDateViewModel(customField, {
                    Single.just(Date())
                })
                CustomFieldType.CHECKBOX -> CustomCheckboxViewModel(customField)
                CustomFieldType.NUMBER -> CustomNumberViewModel(customField)
            }
}

fun CustomField.generateView(context: Context, vm: AbstractCustomViewModel, parent: ViewGroup? = null) =
        CustomFieldViewFactory().generateView(this, vm, context, parent)

fun CustomField.generateViewModel(context: Context? = null) =
        CustomFieldViewFactory().generateViewModel(this, context)
