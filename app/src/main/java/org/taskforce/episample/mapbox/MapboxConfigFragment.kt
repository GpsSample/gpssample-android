package org.taskforce.episample.mapbox

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import com.mapbox.mapboxsdk.offline.*
import kotlinx.android.synthetic.main.fragment_mapbox_config.*
import org.json.JSONObject
import org.taskforce.episample.BuildConfig
import org.taskforce.episample.R
import org.taskforce.episample.config.base.ConfigBuildViewModel
import org.taskforce.episample.core.models.MapboxStyleUrl
import org.taskforce.episample.core.models.MapboxStyleUrl.Companion.DEFAULT_MAPBOX_STYLE
import org.taskforce.episample.core.ui.dialogs.AlertDialogFragment
import org.taskforce.episample.databinding.FragmentMapboxConfigBinding
import org.taskforce.episample.toolbar.viewmodels.AppToolbarViewModel
import org.taskforce.episample.toolbar.viewmodels.AppToolbarViewModelFactory
import org.taskforce.episample.utils.latLngBounds

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
    
    private var shouldShowErrorAlert = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_ACCESS_TOKEN)
        // Set up the offlineManager
        offlineManager = OfflineManager.getInstance(requireContext())

        configBuildViewModel = ViewModelProviders.of(requireActivity()).get(ConfigBuildViewModel::class.java)

        viewModel = ViewModelProviders.of(requireActivity(), MapboxConfigViewModelFactory(
                configBuildViewModel.configBuildManager.config.enumerationAreas.latLngBounds,
                resources,
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
                            .styleUrl(configBuildViewModel.configBuildManager.config.mapboxStyle?.urlString
                                    ?: MapboxStyleUrl.DEFAULT_MAPBOX_STYLE)
            )
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_mapbox_config_mapFrame, mapFragment, MAP_FRAGMENT_TAG)
                    .commit()

        } else {
            mapFragment = childFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG) as SupportMapFragment
        }

        mapFragment?.getMapAsync {
            it.moveCamera(CameraUpdateFactory.newLatLngBounds(configBuildViewModel.configBuildManager.config.enumerationAreas.latLngBounds, 1))

            val enumerationAreas = configBuildViewModel.configBuildManager.config.enumerationAreas

            val polygonOptions = enumerationAreas.map {
                PolygonOptions()
                        .fillColor(R.color.enumerationAreaColor)
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
                downloadRegion(getString(R.string.mapbox_region_name),
                        MapboxStyleUrl(viewModel.styleUrl.get() ?: DEFAULT_MAPBOX_STYLE),
                        configBuildViewModel.configBuildManager.config.enumerationAreas.latLngBounds,
                        viewModel.minZoomString.get()?.toDouble() ?: MapboxConfigViewModel.MIN_ZOOM,
                        viewModel.maxZoomString.get()?.toDouble() ?: MapboxConfigViewModel.MAX_ZOOM)
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

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        toolbar?.setNavigationOnClickListener {
            if (fragmentManager?.backStackEntryCount ?: 0 > 0) {
                fragmentManager?.popBackStack()
            } else {
                requireActivity().finish()
            }
        }
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
                if (fragment_mapbox_config_progressBar.visibility != View.VISIBLE) {
                    shouldShowErrorAlert = true
                    startProgress()
                }
                
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
                Log.e(MapboxDownloadFragment.TAG, "onError reason: " + error.reason)
                Log.e(MapboxDownloadFragment.TAG, "onError message: " + error.message)
                
                if (error.reason == OfflineRegionError.REASON_CONNECTION) {
                    Toast.makeText(this@MapboxConfigFragment.requireContext(), 
                            getString(R.string.mapbox_tiles_error_toast), 
                            Toast.LENGTH_SHORT)
                            .show()
                } else {
                    if(shouldShowErrorAlert) {
                        AlertDialogFragment.newInstance(R.string.offline_tile_error_title, R.string.something_went_wrong)
                                .show(requireFragmentManager(), "AlertDialogFragment")
                        shouldShowErrorAlert = false
                    }
                    endProgress(getString(R.string.mapbox_end_progress_success))
                }
            }

            override fun mapboxTileCountLimitExceeded(limit: Long) {
                endProgress(getString(R.string.mapbox_end_progress_success))
                AlertDialogFragment.newInstance(R.string.offline_tile_error_title, R.string.offline_tile_error_tile_count_exceeded_message)
                        .show(requireFragmentManager(), "AlertDialogFragment")
            }
        })

        // Change the region statem
        offlineRegion?.setDownloadState(OfflineRegion.STATE_ACTIVE)
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