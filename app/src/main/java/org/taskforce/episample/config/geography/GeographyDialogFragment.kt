package org.taskforce.episample.config.geography

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.LiveLocationService
import org.taskforce.episample.databinding.FragmentConfigGeographyDialogBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.getCompatColor
import javax.inject.Inject

class GeographyDialogFragment : DialogFragment(), Observer<Pair<LatLng, Float>> {

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var locationService: LiveLocationService

    lateinit var viewModel: GeographyDialogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).component.inject(this)
        viewModel = GeographyDialogViewModel(
                locationService.locationLiveData,
                LanguageService(languageManager),
                requireContext().getCompatColor(R.color.textColorDisabled),
                requireContext().getCompatColor(R.color.colorAccent),
                {
                    dismiss()
                },
                (parentFragment as GeographyFragment).viewModel
        )
        lifecycle.addObserver(locationService)
        viewModel.location.observe(this, this)
    }

    override fun onChanged(t: Pair<LatLng, Float>?) {
        //NOP
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigGeographyDialogBinding.inflate(inflater).apply {
                vm = viewModel
            }.root
}