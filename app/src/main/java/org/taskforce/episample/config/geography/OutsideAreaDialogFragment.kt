package org.taskforce.episample.config.geography

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.databinding.FragmentOutsideAreaDialogBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class OutsideAreaDialogFragment: DialogFragment() {
    
    @Inject 
    lateinit var languageManager: LanguageManager
    
    lateinit var continueSaving: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentOutsideAreaDialogBinding>(inflater, R.layout.fragment_outside_area_dialog, container, false)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        binding.vm = ViewModelProviders.of(this, OutsideAreaDialogViewModelFactory(LanguageService(languageManager),
                {
                    continueSaving()
                }, {
            this@OutsideAreaDialogFragment.dismiss()
        }))
                .get(OutsideAreaDialogViewModel::class.java)
        
        return binding.root
    }
    
    companion object {
        fun newInstance(continueSaving: () -> Unit): DialogFragment {
            val outsideAreaDialogFragment = OutsideAreaDialogFragment()
            outsideAreaDialogFragment.continueSaving = continueSaving
            return outsideAreaDialogFragment
        }
    }
}