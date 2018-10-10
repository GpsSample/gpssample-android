package org.taskforce.episample.config.mapbox

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import com.mapbox.mapboxsdk.offline.*
import kotlinx.android.synthetic.main.fragment_mapbox_config.*
import org.taskforce.episample.BuildConfig
import org.json.JSONObject
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.core.models.MapboxStyleUrl
import org.taskforce.episample.core.models.MapboxStyleUrl.Companion.DEFAULT_MAPBOX_STYLE
import org.taskforce.episample.databinding.FragmentMapboxConfigBinding
import org.taskforce.episample.toolbar.viewmodels.AppToolbarViewModel
import org.taskforce.episample.toolbar.viewmodels.AppToolbarViewModelFactory
import java.nio.charset.Charset
import java.util.*

class MapboxConfigFragment : Fragment() {

    lateinit var viewModel: MapboxConfigViewModel

    lateinit var configBuildViewModel: ConfigBuildViewModel

    private var mapFragment: SupportMapFragment? = null

    lateinit var toolbarViewModel: AppToolbarViewModel

    private var regionSelected: Int = 0
    private var isEndNotified: Boolean = false

    // Offline objects
    private lateinit var offlineManager: OfflineManager
    private var offlineRegion: OfflineRegion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_ACCESS_TOKEN)
        // Set up the offlineManager
        offlineManager = OfflineManager.getInstance(requireContext())

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        viewModel = ViewModelProviders.of(requireActivity(), MapboxConfigViewModelFactory(
                configBuildViewModel.configBuildManager))
                .get(MapboxConfigViewModel::class.java)

        toolbarViewModel = ViewModelProviders.of(this,
                AppToolbarViewModelFactory(
                        requireActivity().application,
                        R.string.mapbox_config_title,
                        // A config enumeration subject isn't created nor needed at this point
                        org.taskforce.episample.db.config.EnumerationSubject("any", "any", "any", "any")))
                .get(AppToolbarViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentMapboxConfigBinding>(inflater, R.layout.fragment_mapbox_config, container, false)

        binding.vm = viewModel
        binding.toolbarVm = toolbarViewModel
        binding.setLifecycleOwner(this)
//        setHasOptionsMenu(true)

        val toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            mapFragment = SupportMapFragment.newInstance(
                    MapboxMapOptions()
                            .styleUrl(configBuildViewModel.configBuildManager.config.mapboxStyle?.urlString ?: MapboxStyleUrl.DEFAULT_MAPBOX_STYLE)
            )
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_mapbox_config_mapFrame, mapFragment, MAP_FRAGMENT_TAG)
                    .commit()

        } else {
            mapFragment = childFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG) as SupportMapFragment
        }

        mapFragment?.getMapAsync {
            it.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 1))

            val enumerationAreas = configBuildViewModel.configBuildManager.config.enumerationAreas

            val polygonOptions = enumerationAreas.map {
                PolygonOptions()
                        .addAll(it.points.map { (lat, lng) ->
                            LatLng(lat, lng)
                        })
            }
            it.addPolygons(polygonOptions)
        }

        fragment_mapbox_config_testStyle.setOnClickListener {
            viewModel.styleUrl.get()?.let { style ->
                mapFragment?.getMapAsync {
                    it.setStyle(style)
                }
            }
        }

        fragment_mapbox_config_downloadButton.setOnClickListener {
            mapFragment?.getMapAsync { map ->
                downloadRegion(getString(R.string.mapbox_region_name), MapboxStyleUrl(viewModel.styleUrl.get()
                        ?: DEFAULT_MAPBOX_STYLE),
                        latLngBounds, map.cameraPosition.zoom, map.cameraPosition.zoom + 1)
            }
        }

        offlineManager?.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                viewModel.offlineRegions.set(offlineRegions?.toList() ?: listOf())
            }

            override fun onError(error: String?) {
                viewModel.offlineRegions.set(listOf())
            }
        })

        mapFragment?.onCreate(savedInstanceState)
    }

//    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
//        inflater?.inflate(R.menu.navigation, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            R.id.app_action_help -> {
//                return super.onOptionsItemSelected(item)
//            }
//            R.id.app_action_language -> {
//                toolbarViewModel.languageSelectVisibility.postValue(true)
//            }
//        }
//        return true
//    }

    private val latLngBounds: LatLngBounds
        get() {
            val enumerationAreas = configBuildViewModel.configBuildManager.config.enumerationAreas

            val allLatsAscending = enumerationAreas.map { it.points.map { it.first } }.flatMap { it }.sorted()
            val allLongsAscending = enumerationAreas.map { it.points.map { it.second } }.flatMap { it }.sorted()

            val south = allLatsAscending.first()
            val north = allLatsAscending.last()
            val west = allLongsAscending.first()
            val east = allLongsAscending.last()

            return LatLngBounds.from(north, east, south, west)
        }

    private fun downloadRegion(regionName: String, styleUrl: MapboxStyleUrl,
                               latLngBounds: LatLngBounds, minZoom: Double, maxZoom: Double) {

        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                offlineRegions?.forEach {
                    it.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                        override fun onDelete() {

                        }
                        override fun onError(error: String) {

                        }
                    })
                }
            }

            override fun onError(error: String?) {

            }
        })

        // Define offline region parameters, including bounds,
        // min/max zoom, and metadata

        // Start the fragment_mapbox_config_progressBar
        startProgress()

        // Create offline definition using the current
        // style and boundaries of visible map area
        val bounds = latLngBounds
        val pixelRatio = this.resources.displayMetrics.density
        val definition = OfflineTilePyramidRegionDefinition(
                styleUrl.urlString, bounds, minZoom, maxZoom, pixelRatio)

        // Build a JSONObject using the user-defined offline region title,
        // convert it into string, and use it to create a metadata variable.
        // The metadata variable will later be passed to createOfflineRegion()
        var metadata: ByteArray?
        try {
            val jsonObject = JSONObject()
            jsonObject.put(JSON_FIELD_REGION_NAME, regionName)
            val json = jsonObject.toString()
            metadata = json.toByteArray(charset(JSON_CHARSET))
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.message)
            metadata = null
        }

        // Create the offline region and launch the download
        offlineManager?.createOfflineRegion(definition, metadata!!, object : OfflineManager.CreateOfflineRegionCallback {
            override fun onCreate(offlineRegion: OfflineRegion) {
                Log.d(TAG, "Offline region created: $regionName")
                this@MapboxConfigFragment.offlineRegion = offlineRegion
                launchDownload()
            }

            override fun onError(error: String) {
                Log.e(TAG, "Error: $error")
            }
        })
    }

    private fun launchDownload() {
        // Set up an observer to handle download progress and
        // notify the user when the region is finished downloading
        offlineRegion?.setObserver(object : OfflineRegion.OfflineRegionObserver {
            override fun onStatusChanged(status: OfflineRegionStatus) {
                // Compute a percentage
                val percentage = if (status.requiredResourceCount >= 0)
                    100.0 * status.completedResourceCount / status.requiredResourceCount
                else
                    0.0

                if (status.isComplete) {
                    // Download complete
                    endProgress(getString(R.string.mapbox_end_progress_success))
                    offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
                        override fun onList(offlineRegions: Array<OfflineRegion>?) {
                            viewModel.offlineRegions.set(offlineRegions?.toList() ?: listOf())
                        }

                        override fun onError(error: String?) {

                        }
                    })
                            return
                } else if (status.isRequiredResourceCountPrecise) {
                    // Switch to determinate state
                    setPercentage(Math.round(percentage).toInt())
                }

                // Log what is being currently downloaded
                Log.d(TAG, String.format("%s/%s resources; %s bytes downloaded.",
                        status.completedResourceCount.toString(),
                        status.requiredResourceCount.toString(),
                        status.completedResourceSize.toString()))
            }

            override fun onError(error: OfflineRegionError) {
                Log.e(TAG, "onError reason: " + error.reason)
                Log.e(TAG, "onError message: " + error.message)
            }

            override fun mapboxTileCountLimitExceeded(limit: Long) {
                Log.e(TAG, "Mapbox tile count limit exceeded: $limit")
            }
        })

        // Change the region statem
        offlineRegion?.setDownloadState(OfflineRegion.STATE_ACTIVE)
    }

    private fun downloadedRegionList() {
        // Build a region list when the user clicks the list button

        // Reset the region selected int to 0
        regionSelected = 0

        // Query the DB asynchronously
        offlineManager?.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                // Check result. If no regions have been
                // downloaded yet, notify user and return
                if (offlineRegions == null || offlineRegions.size == 0) {
                    // TODO
//                    Toast.makeText(, getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show()
                    return
                }

                // Add all of the region names to a list
                val offlineRegionsNames = ArrayList<String>()
                for (offlineRegion in offlineRegions) {
                    offlineRegionsNames.add(getRegionName(offlineRegion))
                }
                val items = offlineRegionsNames.toTypedArray<CharSequence>()

                // Build a dialog containing the list of regions
                val dialog = AlertDialog.Builder(requireActivity())
                        .setTitle(getString(R.string.mapbox_navigate_title))
                        .setSingleChoiceItems(items, 0) { dialog1, which ->
                            // Track which region the user selects
                            regionSelected = which
                        }
                        .setPositiveButton(getString(R.string.mapbox_navigate_positive_button)) { _, _ ->

                            // Get the region bounds and zoom
                            val bounds = (offlineRegions[regionSelected].definition as OfflineTilePyramidRegionDefinition).bounds
                            val regionZoom = (offlineRegions[regionSelected].definition as OfflineTilePyramidRegionDefinition).minZoom

                            // Create new camera position
                            val cameraPosition = CameraPosition.Builder()
                                    .target(bounds.center)
                                    .zoom(regionZoom)
                                    .build()

                            // Move camera to new position

                            // TODO
//                            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                        }
                        .setNeutralButton(getString(R.string.mapbox_navigate_neutral_button_title)) { dialog13, id ->
                            // Make fragment_mapbox_config_progressBar indeterminate and
                            // set it to visible to signal that
                            // the deletion process has begun
                            fragment_mapbox_config_progressBar.isIndeterminate = true
                            fragment_mapbox_config_progressBar.visibility = View.VISIBLE

                            // Begin the deletion process
                            offlineRegions[regionSelected].delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                                override fun onDelete() {
                                    // Once the region is deleted, remove the
                                    // fragment_mapbox_config_progressBar and display a toast
                                    fragment_mapbox_config_progressBar.visibility = View.INVISIBLE
                                    fragment_mapbox_config_progressBar.isIndeterminate = false
//                                    Toast.makeText(getApplicationContext(), getString(R.string.toast_region_deleted),
//                                            Toast.LENGTH_LONG).show()
                                }

                                override fun onError(error: String) {
                                    fragment_mapbox_config_progressBar.visibility = View.INVISIBLE
                                    fragment_mapbox_config_progressBar.isIndeterminate = false
                                    Log.e(TAG, "Error: $error")
                                }
                            })
                        }
                        .setNegativeButton(getString(R.string.mapbox_navigate_negative_button_title)) { dialog, id ->
                            // When the user cancels, don't do anything.
                            // The dialog will automatically close
                        }.create()
                dialog.show()

            }

            override fun onError(error: String) {
                Log.e(TAG, "Error: $error")
            }
        })
    }

    private fun getRegionName(offlineRegion: OfflineRegion): String {
        // Get the region name from the offline region metadata
        return try {
            val metadata = offlineRegion.metadata
            val json = String(metadata, Charset.forName(JSON_CHARSET))
            val jsonObject = JSONObject(json)
            jsonObject.getString(JSON_FIELD_REGION_NAME)
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to decode metadata: " + exception.message)
            return getString(R.string.mapbox_region_name)
        }
    }

    // Progress bar methods
    private fun startProgress() {
        // Disable buttons
        fragment_mapbox_config_downloadButton.isEnabled = false
//        fragment_mapbox_config_listButton.isEnabled = false

        // Start and show the progress bar
        isEndNotified = false
        fragment_mapbox_config_progressBar.isIndeterminate = true
        fragment_mapbox_config_progressBar.visibility = View.VISIBLE
    }

    private fun setPercentage(percentage: Int) {
        fragment_mapbox_config_progressBar.isIndeterminate = false
        fragment_mapbox_config_progressBar.progress = percentage
    }

    private fun endProgress(message: String) {
        // Don't notify more than once
        if (isEndNotified) {
            return
        }

        // Enable buttons
        fragment_mapbox_config_downloadButton.isEnabled = true
//        fragment_mapbox_config_listButton.isEnabled = true

        // Stop and hide the progress bar
        isEndNotified = true
        fragment_mapbox_config_progressBar.isIndeterminate = false
        fragment_mapbox_config_progressBar.visibility = View.GONE

        // TODO
        // notify complete
    }


    companion object {
        const val TAG = "MapboxConfigFragment"
        const val JSON_CHARSET = "UTF-8"
        const val JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME"
        const val MAP_FRAGMENT_TAG = "mapboxConfigFragment.MapboxFragment"
        const val HELP_TARGET = "#geography"
    }
}