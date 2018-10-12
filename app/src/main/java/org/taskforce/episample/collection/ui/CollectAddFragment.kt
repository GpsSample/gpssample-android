package org.taskforce.episample.collection.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_collect_add.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.MapboxItemMarkerManager
import org.taskforce.episample.collection.managers.generateView
import org.taskforce.episample.collection.managers.generateViewModel
import org.taskforce.episample.collection.viewmodels.CollectAddViewModel
import org.taskforce.episample.collection.viewmodels.CollectAddViewModelFactory
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.collection.viewmodels.CustomDropdownViewModel
import org.taskforce.episample.config.geography.OutsideAreaDialogFragment
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.core.ui.dialogs.DatePickerFragment
import org.taskforce.episample.core.ui.dialogs.TimePickerFragment
import org.taskforce.episample.core.util.FileUtil
import org.taskforce.episample.databinding.FragmentCollectAddBinding
import org.taskforce.episample.db.config.customfield.CustomDateType
import org.taskforce.episample.db.config.customfield.metadata.DateMetadata
import org.taskforce.episample.help.HelpActivity
import org.taskforce.episample.mapbox.MapboxLayersFragment
import org.taskforce.episample.navigation.ui.NavigationToolbarViewModel
import org.taskforce.episample.navigation.ui.NavigationToolbarViewModelFactory
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.toolbar.viewmodels.ToolbarViewModel
import org.taskforce.episample.utils.getCompatColor
import org.taskforce.episample.utils.loadImage
import org.taskforce.episample.utils.toMapboxLatLng
import java.io.IOException
import java.util.*
import javax.inject.Inject

class CollectAddFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService

    @Inject
    lateinit var config: Config

    lateinit var collectViewModel: CollectAddViewModel
    lateinit var navigationToolbarViewModel: NavigationToolbarViewModel

    var imageUri: Uri? = null

    private var mapFragment: SupportMapFragment? = null
    private val markerManagerLiveData = MutableLiveData<MapboxItemMarkerManager>()

    private val mapPreferences: SharedPreferences
        get() = requireActivity().getSharedPreferences(MAP_PREFERENCE_NAMESPACE, Context.MODE_PRIVATE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity().application as EpiApplication).collectComponent?.inject(this)

        languageService = LanguageService(languageManager)
        collectViewModel = ViewModelProviders.of(this@CollectAddFragment, CollectAddViewModelFactory(
                requireActivity().application,
                languageService,
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
                },
                { photoUri ->
                    val photoFragment = ViewPhotoFragment.newInstance(photoUri)
                    photoFragment.show(requireFragmentManager(), ViewPhotoFragment::class.java.simpleName)
                }
        )).get(CollectAddViewModel::class.java)


        val viewModelFactory = if (arguments?.getBoolean(IS_LANDMARK) == true) {
            NavigationToolbarViewModelFactory(
                    requireActivity().application,
                    R.string.collect_add_landmark_title)
        } else {
            NavigationToolbarViewModelFactory(
                    requireActivity().application,
                    R.string.collect_add_item_title,
                    collectViewModel.config.enumerationSubject.singular.capitalize()
            )
        }

        navigationToolbarViewModel = ViewModelProviders.of(this,
                viewModelFactory
        ).get(NavigationToolbarViewModel::class.java)

    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentCollectAddBinding.inflate(inflater).apply {

            vm = collectViewModel
            toolbarVm = navigationToolbarViewModel

            setHasOptionsMenu(true)
            val toolbar = root.findViewById<Toolbar>(R.id.toolbar)
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)



            lifecycle.addObserver(collectViewModel.locationService)

            landmarkImageSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val landmarkType = collectViewModel.config.landmarkTypes[position]
                    collectViewModel.selectedLandmark.postValue(landmarkType)
                    collectViewModel.landmarkType = landmarkType
                }
            }

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

        collectViewModel.enumerations.observe(this, Observer {
            it?.let {
                val mostRecent = it.sortedByDescending { it.dateCreated }.firstOrNull()
                collectViewModel.mostRecentEnumeration = mostRecent
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            mapFragment = SupportMapFragment.newInstance(MapboxMapOptions().styleUrl(collectViewModel.config.mapboxStyle.urlString))
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.collectAddMap, mapFragment, MAP_FRAGMENT_TAG)
                    .commit()

        } else {
            mapFragment = childFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG) as SupportMapFragment
        }

        mapFragment?.getMapAsync { map ->
            markerManagerLiveData.postValue(MapboxItemMarkerManager(requireContext(),
                    mapPreferences,
                    map))

            collectViewModel.gpsBreadcrumbs.observe(this@CollectAddFragment, Observer { breadcrumbs ->
                val polylineOptions = mutableListOf<PolylineOptions>()
                breadcrumbs?.sortedBy { it.dateCreated }?.forEach {
                    if (it.startOfSession) {
                        polylineOptions.add(PolylineOptions()
                                .color(R.color.greyHighlights))
                    }

                    polylineOptions.last().add(it.location.toMapboxLatLng())
                }

                polylineOptions.forEach { breadCrumbPath ->
                    map.addPolyline(breadCrumbPath)
                }
            })

            LiveDataPair(markerManagerLiveData, collectViewModel.collectItems).observe(this, Observer {
                it?.let { (markerManager, items) ->
                    markerManager.addMarkerDiff(items)
                }
            })

            LiveDataPair(markerManagerLiveData, collectViewModel.locationService.locationLiveData).observe(this@CollectAddFragment, Observer {
                it?.let { (markerManager, locationPair) ->
                    locationPair.let { (latLng, accuracy) ->
                        var cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng.toMapboxLatLng(), CollectViewModel.zoomLevel)
                        markerManager.setCurrentLocation(latLng.toMapboxLatLng(), accuracy.toDouble())
                        if (!collectViewModel.useDuplicatedGps) { // Only update if not using the duplicated GPS
                            collectViewModel.gpsDisplay.set("%.5f ".format(latLng.latitude) +
                                    ", %.5f".format(latLng.longitude))
                        }

                        if (!collectViewModel.useDuplicatedGps) {
                            if (accuracy <= collectViewModel.locationPrecision ?: 1000f) {
                                collectViewModel.locationPrecision = accuracy
                                collectViewModel.locationLatLng = latLng

                                collectViewModel.locationLatLng?.let {
                                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(it.toMapboxLatLng(), CollectAddViewModel.mapZoom)
                                }
                                collectViewModel.location.postValue(latLng)
                                collectViewModel.gpsDisplay.set("%.5f ".format(latLng.latitude) + ", %.5f".format(latLng.longitude))
                                collectViewModel.gpsVm?.precision?.set(accuracy.toDouble())
                            }
                        }

                        map.moveCamera(cameraUpdate)
                    }
                }

            })
        }

        val types = collectViewModel.landmarkTypes

        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(types.map { it.name })

        landmarkImageSelector.adapter = adapter

        mapFragment?.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        markerManagerLiveData.observe(this, Observer {
            it?.let {
                it.applyLayerSettings()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.map, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.center_my_location -> {
                mapFragment?.getMapAsync {
                    markerManagerLiveData.value?.getCurrentLocation()?.let { currentLocation ->
                        it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, CollectViewModel.zoomLevel))
                    } ?: run {
                        Toast.makeText(requireContext(), R.string.current_location_unknown, Toast.LENGTH_LONG).show()
                    }
                }
            }
            R.id.toggle_breadcrumbs -> {
                markerManagerLiveData?.value?.let {
                    it.toggleBreadcrumbs()
                }
            }
            R.id.toggle_layers -> {
                // access map on the main thread
                markerManagerLiveData?.value?.mapboxMap?.let { map ->
                    val mapLayersFragment = MapboxLayersFragment.newInstance(map.cameraPosition.target, map.cameraPosition.zoom, CollectFragment.MAP_PREFERENCE_NAMESPACE)
                    requireFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainFrame, mapLayersFragment)
                            .addToBackStack(MapboxLayersFragment::class.java.name)
                            .commit()
                }
            }
            R.id.action_help -> {
                HelpActivity.startActivity(requireContext(), "https://github.com/EpiSample/episample-android/wiki/Welcome")
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            GET_DATE_CODE -> {
                if (data == null) {
                    return
                }
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
                if (data == null) {
                    return
                }
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
        const val MAP_PREFERENCE_NAMESPACE = "SHARED_MAPBOX_LAYER_PREFERENCES"
        const val MAP_FRAGMENT_TAG = "collectAddFragment.MapboxFragment"

        const val IS_LANDMARK = "isLandmark"
        const val HELP_TARGET = "#collectAdd"
        const val GET_TIME_CODE = 1
        const val GET_DATE_CODE = 2
        const val TAKE_PICTURE = 9457
    }
}