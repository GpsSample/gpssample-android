package org.taskforce.episample.collection.ui

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.viewmodels.DuplicateGpsDialogViewModel
import org.taskforce.episample.collection.viewmodels.DuplicateGpsDialogViewModelFactory
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.databinding.FragmentDuplicateGpsDialogBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.closeKeyboard
import javax.inject.Inject

class DuplicateGpsDialogFragment: DialogFragment() {
    
    @Inject
    lateinit var languageManager: LanguageManager
    
    lateinit var enumerationSubject: String
    lateinit var enumerationTitle: String
    lateinit var location: LatLng
    
    lateinit var useLastRecorded: () -> Unit
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)
        
        enumerationSubject = arguments!!.getString(ARG_ENUM_SUBJECT)
        enumerationTitle = arguments!!.getString(ARG_ENUM_NAME)
        
        val latitude = arguments!!.getDouble(ARG_ENUM_LATITUDE)
        val longitude = arguments!!.getDouble(ARG_ENUM_LONGITUDE)
        location = LatLng(latitude, longitude)
        
        setStyle(DialogFragment.STYLE_NORMAL, R.style.PickerDialogCustom)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentDuplicateGpsDialogBinding>(inflater, R.layout.fragment_duplicate_gps_dialog, container, false)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        binding.vm = ViewModelProviders.of(this, DuplicateGpsDialogViewModelFactory(LanguageService(languageManager),
                enumerationTitle,
                enumerationSubject,
                location.latitude,
                location.longitude,
                {
                    this@DuplicateGpsDialogFragment.dismiss()
                }, {
            useLastRecorded()
        })).get(DuplicateGpsDialogViewModel::class.java)
        
        binding.setLifecycleOwner(this)
        
        return binding.root
    }

    companion object {
        private const val ARG_ENUM_SUBJECT = "ARG_ENUM_SUBJECT"
        private const val ARG_ENUM_NAME = "ARG_ENUM_NAME"
        private const val ARG_ENUM_LATITUDE = "ARG_ENUM_LATITUDE"
        private const val ARG_ENUM_LONGITUDE = "ARG_ENUM_LONGITUDE"

        fun newInstance(enumeration: Enumeration, subject: EnumerationSubject, useLastRecorded: () -> Unit): DialogFragment {
            val fragment = DuplicateGpsDialogFragment()
            fragment.arguments = Bundle()
            fragment.arguments!!.putString(ARG_ENUM_SUBJECT, subject.singular.toLowerCase())
            fragment.arguments!!.putString(ARG_ENUM_NAME, enumeration.title)
            fragment.arguments!!.putDouble(ARG_ENUM_LATITUDE, enumeration.location.latitude)
            fragment.arguments!!.putDouble(ARG_ENUM_LONGITUDE, enumeration.location.longitude)
            
            fragment.useLastRecorded = useLastRecorded
            
            return fragment
        }
    }
}