package org.taskforce.episample.config.landmark

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.transfer.TransferFileBucket
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.config.transfer.TransferViewModel
import org.taskforce.episample.databinding.FragmentConfigLandmarksAddBinding
import org.taskforce.episample.fileImport.models.LandmarkType
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.closeKeyboard
import org.taskforce.episample.utils.getCompatColor
import javax.inject.Inject

class LandmarkAddFragment : Fragment() {

    @Inject
    lateinit var transferManager: TransferManager

    @Inject
    lateinit var landmarkTypeManager: LandmarkTypeManager

    @Inject
    lateinit var languageManager: LanguageManager

    var landmarkTypeToEdit: LandmarkType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentConfigLandmarksAddBinding.inflate(inflater).apply {
                val adapter = LandmarkAddAdapter(requireContext())
                adapter.preSelectedIcon = landmarkTypeToEdit?.iconLocation
              
                landmarkTypeManager.landmarksIconObservable.subscribe(adapter)
                landmarkIconGrid.layoutManager = GridLayoutManager(requireContext(), 5)
                landmarkIconGrid.adapter = adapter

                val landmarkAddViewModel = LandmarkAddViewModel(
                        LanguageService(languageManager),
                        requireContext().getCompatColor(R.color.textColorInverse),
                        requireContext().getCompatColor(R.color.textColorInverseDisabled),
                        adapter.selectedObservable,
                        landmarkTypeManager, {

                    requireActivity().supportFragmentManager.popBackStack()
                    requireActivity().currentFocus.closeKeyboard()
                })
                landmarkAddViewModel.loadLandmarkTypeToEdit(landmarkTypeToEdit)
                
                vm = landmarkAddViewModel
                transferVm = TransferViewModel(
                        LanguageService(languageManager),
                        transferManager,
                        childFragmentManager, TransferFileBucket.LANDMARK).apply {
                    showFileList = false
                }
            }.root
}