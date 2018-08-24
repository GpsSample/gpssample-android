package org.taskforce.episample.config.fields

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.databinding.FragmentConfigFieldsDropdownDialogBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class CustomFieldsAddDropdownDialog : DialogFragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigFieldsDropdownDialogBinding.inflate(inflater).apply {
                arguments?.let {
                    vm = CustomFieldsAddDropdownDialogViewModel(
                            languageManager.getString(R.string.config_fields_add_dropdown_dialog_title),
                            languageManager.getString(R.string.config_fields_add_dropdown_dialog_hint),
                            languageManager.getString(R.string.cancel),
                            languageManager.getString(R.string.config_done),
                            {
                                dismiss()
                            })
                }
            }.root
}