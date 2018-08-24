package org.taskforce.episample.config.geography

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentConfigGeographyDialogBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.getCompatColor
import javax.inject.Inject

class GeographyDialogFragment : DialogFragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    lateinit var viewModel: GeographyDialogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)

        viewModel = GeographyDialogViewModel(
                LanguageService(languageManager),
                requireContext().getCompatColor(R.color.textColorDisabled),
                requireContext().getCompatColor(R.color.colorAccent),
                {
                    dismiss()
                },
                (parentFragment as GeographyFragment).viewModel
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigGeographyDialogBinding.inflate(inflater).apply {
                vm = viewModel
            }.root
}