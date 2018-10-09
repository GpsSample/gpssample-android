package org.taskforce.episample.collection.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_collect_add.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.CollectionItemMarkerManager
import org.taskforce.episample.collection.managers.generateView
import org.taskforce.episample.collection.managers.generateViewModel
import org.taskforce.episample.collection.viewmodels.CollectAddViewModel
import org.taskforce.episample.collection.viewmodels.CollectAddViewModelFactory
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.collection.viewmodels.CustomDropdownViewModel
import org.taskforce.episample.config.geography.OutsideAreaDialogFragment
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.core.ui.dialogs.DatePickerFragment
import org.taskforce.episample.core.ui.dialogs.TimePickerFragment
import org.taskforce.episample.core.util.FileUtil
import org.taskforce.episample.databinding.FragmentCollectAddBinding
import org.taskforce.episample.db.config.customfield.CustomDateType
import org.taskforce.episample.db.config.customfield.metadata.DateMetadata
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import org.taskforce.episample.utils.getCompatColor
import org.taskforce.episample.utils.loadImage
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class CollectAddFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService
    
    @Inject
    lateinit var config: Config

    lateinit var collectIconFactory: CollectIconFactory
    lateinit var markerManager: CollectionItemMarkerManager

    lateinit var collectViewModel: CollectAddViewModel
    
    var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).collectComponent?.inject(this)

        collectIconFactory = CollectIconFactory(requireContext().resources)
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mapFragment = SupportMapFragment()

        val binding = FragmentCollectAddBinding.inflate(inflater).apply {
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.collectAddMap, mapFragment)
                    .commit()

            mapFragment.getMapAsync {
                markerManager = CollectionItemMarkerManager(collectIconFactory, it)

                it.isMyLocationEnabled = true
                it.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }

            languageService = LanguageService(languageManager)

            collectViewModel = ViewModelProviders.of(this@CollectAddFragment, CollectAddViewModelFactory(
                    requireActivity().application,
                    languageService,
                    Single.create<GoogleMap> { single ->
                        mapFragment.getMapAsync {
                            single.onSuccess(it)
                        }
                    },
                    arguments?.getBoolean(IS_LANDMARK) == true,
                    requireContext().getCompatColor(R.color.colorAccent),
                    requireContext().getCompatColor(R.color.textColorDisabled),
                    requireContext().getCompatColor(R.color.textColorInverse),
                    requireContext().getCompatColor(R.color.textColorDetails),
                    {
                        requireFragmentManager()
                                .popBackStack()
                    }, {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                        val newPhoto = try {
                            FileUtil.createImageFile(requireContext())
                        } catch (ex: IOException) {
                            null
                        }
                        
                        newPhoto?.also {
                            val photoUri = FileProvider.getUriForFile(requireContext(), 
                                    "org.taskforce.episample.fileprovider",
                                    it)
                            imageUri = photoUri
                            
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                            startActivityForResult(takePictureIntent, TAKE_PICTURE)
                        }
                    }
                }
            },
                    { enumeration, subject ->
                        enumeration?.let {
                            val duplicateDialog = DuplicateGpsDialogFragment.newInstance(enumeration, subject) {
                                collectViewModel.duplicateGps(enumeration)
                            }
                            duplicateDialog.show(childFragmentManager, DuplicateGpsDialogFragment::class.java.simpleName)
                        }
                    },
                    { latLng, precision ->
                        val outsideAreaDialog = OutsideAreaDialogFragment.newInstance {
                            collectViewModel.saveEnumeration(latLng, precision, shouldExclude = true)
                        }
                        outsideAreaDialog.show(childFragmentManager, OutsideAreaDialogFragment::class.java.simpleName)
                    }
            )).get(CollectAddViewModel::class.java)

            lifecycle.addObserver(collectViewModel.locationService)
            collectViewModel.locationService.locationLiveData.observe(this@CollectAddFragment, Observer {
                it?.let { (latLng, accuracy) ->

                    collectViewModel.googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, CollectViewModel.zoomLevel))

                    if (!collectViewModel.useDuplicatedGps) { // Only update if not using the duplicated GPS
                        collectViewModel.gpsDisplay.set("%.5f ".format(latLng.latitude) +
                                ", %.5f".format(latLng.longitude))
                    }

                    if (!collectViewModel.useDuplicatedGps) {
                        if (accuracy < collectViewModel.locationPrecision ?: 1000f) {
                            collectViewModel.lastKnownLocation = latLng
                            collectViewModel.locationPrecision = accuracy
                            collectViewModel.locationLatLng = latLng

                            collectViewModel.locationLatLng?.let {
                                collectViewModel.googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(it, CollectAddViewModel.mapZoom))
                            }
                            collectViewModel.location.postValue(latLng)
                            collectViewModel.gpsDisplay.set("%.5f ".format(latLng.latitude) + ", %.5f".format(latLng.longitude))
                            gpsVm?.precision?.set(accuracy.toDouble())
                        }
                    }
                }
            })

            landmarkImageSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val landmarkType = collectViewModel.config.landmarkTypes[position]
                    collectViewModel.selectedLandmark.postValue(landmarkType)
                }
            }

            vm = collectViewModel

            toolbarVm = ToolbarViewModel(
                    LanguageService(languageManager),
                    languageManager,
                    HELP_TARGET, {
                requireActivity().supportFragmentManager.popBackStack()
            })

            collectViewModel.customFields.forEach {
                if (!it.isAutomatic) {
                    it.generateViewModel(collectViewModel.config.displaySettings,
                            {
                                showDatePicker(it)
                            },
                            requireContext()).apply {
                        val view = it.generateView(root, requireContext(), this, customFieldHolder)
                        if (this is CustomDropdownViewModel) {
                            this.view = view
                        }
                        collectViewModel.addCustomFieldViewModel(this)
                        customFieldHolder.addView(
                                view,
                                LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ))
                    }
                }
            }

            val userSettings = collectViewModel.config.userSettings
            val vm = CollectGpsPrecisionViewModel(userSettings.gpsMinimumPrecision,
                    userSettings.gpsPreferredPrecision,
                    requireContext().getCompatColor(R.color.colorError),
                    requireContext().getCompatColor(R.color.colorWarning),
                    requireContext().getCompatColor(R.color.gpsAcceptable))

            gpsVm = vm

            collectViewModel.gpsVm = vm
        }
        binding.setLifecycleOwner(this)

        collectViewModel.gpsBreadcrumbs.observe(this@CollectAddFragment, Observer { breadcrumbs ->
            mapFragment.getMapAsync { map ->
                val polylineOptions= mutableListOf<PolylineOptions>()
                breadcrumbs?.sortedBy { it.dateCreated }?.forEach {
                    if (it.startOfSession) {
                        polylineOptions.add(PolylineOptions()
                                .clickable(false)
                                .color(R.color.greyHighlights))
                    }

                    polylineOptions.last().add(it.location)
                }

                polylineOptions.forEach { breadCrumbPath ->
                    map.addPolyline(breadCrumbPath)
                }
            }
        })

        collectViewModel.collectItems.observe(this, Observer { items ->
            mapFragment.getMapAsync {
                markerManager.addMarkerDiff(items ?: emptyList())
            }
        })

        collectViewModel.enumerations.observe(this, Observer {
            it?.let {
                val mostRecent = it.sortedByDescending { it.dateCreated }.firstOrNull()
                collectViewModel.mostRecentEnumeration = mostRecent
            }
        })

        val enumerationSubject = collectViewModel.config.enumerationSubject
        binding.toolbarVm?.title = if (arguments?.getBoolean(IS_LANDMARK) == true) {
            languageManager.getString(R.string.collect_add_landmark_title)
        } else {
            languageManager.getString(R.string.collect_add_item_title, enumerationSubject.singular.capitalize())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val types = collectViewModel.landmarkTypes ?: emptyList()

        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(types.map { it.name })

        landmarkImageSelector.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            GET_DATE_CODE -> {
                if (data == null) { return }
                val customFieldId = data.getStringExtra(DatePickerFragment.EXTRA_CUSTOM_FIELD_ID)
                val year = data.getIntExtra(DatePickerFragment.EXTRA_YEAR, 0)
                val month = data.getIntExtra(DatePickerFragment.EXTRA_MONTH, 1) + 1
                val day = data.getIntExtra(DatePickerFragment.EXTRA_DAY_OF_MONTH, 0)
                val showTimeNext = data.getBooleanExtra(DatePickerFragment.EXTRA_SHOW_TIME_PICKER_AFTER, false)

                val cal = Calendar.getInstance()
                cal.set(year, month, day)

                if (showTimeNext) {
                    showTimePickerDialog(customFieldId, cal.time)
                } else {
                    collectViewModel.updateDateField(customFieldId, cal.time)
                }
            }
            GET_TIME_CODE -> {
                if (data == null) { return }
                val customFieldId = data.getStringExtra(TimePickerFragment.EXTRA_CUSTOM_FIELD_ID)
                val chosenDateLong = data.getLongExtra(TimePickerFragment.EXTRA_CHOSEN_DATE, 0)
                val hour = data.getIntExtra(TimePickerFragment.EXTRA_HOUR, 0)
                val minute = data.getIntExtra(TimePickerFragment.EXTRA_MINUTE, 0)
                val date = Date(chosenDateLong)

                val cal = Calendar.getInstance()
                cal.time = date
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                collectViewModel.updateDateField(customFieldId, cal.time)
            }
            TAKE_PICTURE -> {
                // TODO: scale the image so it doesn't take much memory
                imageUri?.let { photoUri ->
                    collectViewModel.setImage(photoUri)
                    
                    config.userSettings.photoCompressionScale?.let { compressionScale ->
                        FileUtil.compressBitmap(requireContext(), photoUri, compressionScale)
                    }
                    
                    collectAddImage.loadImage(photoUri.toString(), 
                            requireContext().getDrawable(R.drawable.photo_empty))
                }
            }
        }
    }

    private fun showDatePicker(customField: CustomField) {
        (customField.metadata as? DateMetadata)?.let { dateMetadata ->
            when (dateMetadata.dateType) {
                CustomDateType.DATE -> showDatePickerDialog(customField, false)
                CustomDateType.TIME -> showTimePickerDialog(customField.id)
                CustomDateType.DATE_TIME -> showDatePickerDialog(customField, true)
            }
        }
    }

    private fun showDatePickerDialog(customField: CustomField, showTimePickerAfter: Boolean = false) {
        val newFragment = DatePickerFragment.newInstance(customField.id, showTimePickerAfter)
        newFragment.setTargetFragment(this, GET_DATE_CODE)
        newFragment.show(fragmentManager, "datePicker")
    }

    private fun showTimePickerDialog(customFieldId: String, chosenDate: Date? = null) {
        val newFragment = TimePickerFragment.newInstance(customFieldId, chosenDate)
        newFragment.setTargetFragment(this, GET_TIME_CODE)
        newFragment.show(fragmentManager, "timePicker")
    }

    companion object {
        const val IS_LANDMARK = "isLandmark"
        const val HELP_TARGET = "#collectAdd"
        const val GET_TIME_CODE = 1
        const val GET_DATE_CODE = 2
        const val TAKE_PICTURE = 9457
    }
}