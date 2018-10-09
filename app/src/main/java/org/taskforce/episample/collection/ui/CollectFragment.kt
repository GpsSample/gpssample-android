package org.taskforce.episample.collection.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_collect.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.MapboxItemMarkerManager
import org.taskforce.episample.collection.viewmodels.CollectCardViewModel
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.collection.viewmodels.CollectViewModelFactory
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.databinding.FragmentCollectBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.getCompatColor
import org.taskforce.episample.utils.inflater
import org.taskforce.episample.utils.toMapboxLatLng
import javax.inject.Inject

class CollectFragment : Fragment(), MapboxMap.OnMarkerClickListener, MapboxMap.OnMapClickListener {

    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService

    lateinit var collectIconFactory: CollectIconFactory

    lateinit var collectViewModel: CollectViewModel

    var adapter: CollectItemAdapter? = null
    lateinit var breadcrumbPath: PolylineOptions
    lateinit var collectCardVm: CollectCardViewModel

    private var mapFragment: SupportMapFragment? = null
    private val markerManagerLiveData = MutableLiveData<MapboxItemMarkerManager>()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))

        languageService = LanguageService(languageManager)

        collectIconFactory = CollectIconFactory(requireContext().resources)

        breadcrumbPath = PolylineOptions()

        collectViewModel = ViewModelProviders.of(this@CollectFragment,
                CollectViewModelFactory(
                        requireActivity().application,
                        languageService,
                        {
                            showCollectAddScreen(it)
                        },
                        {
                            requireActivity().supportFragmentManager.popBackStack()
                        })).get(CollectViewModel::class.java)

        val userSettings = collectViewModel.config.userSettings
        val enumerationSubject = collectViewModel.config.enumerationSubject
        val displaySettings = collectViewModel.config.displaySettings

        collectCardVm = CollectCardViewModel(userSettings,
                enumerationSubject,
                displaySettings,
                requireContext().getCompatColor(R.color.colorError),
                requireContext().getCompatColor(R.color.colorWarning),
                requireContext().getCompatColor(R.color.gpsAcceptable))

        lifecycle.addObserver(collectViewModel.locationService)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentCollectBinding.inflate(inflater.context.inflater).apply {
            vm = collectViewModel
            cardVm = collectCardVm
        }

        adapter = CollectItemAdapter(CollectIconFactory(requireContext().resources),
                languageService.getString(R.string.collect_incomplete),
                collectViewModel.config.displaySettings)
        binding.collectList.adapter = adapter

        LiveDataPair(markerManagerLiveData, collectViewModel.collectItems).observe(this, Observer {
            it?.let { (markerManager, items) ->

                val sortedItems = items.sortedByDescending { it.dateCreated }
                adapter?.data = sortedItems

                val titleText = languageService.getString(R.string.collect_title, "${items.size}")
                collectTitle.text = titleText

                markerManager.addMarkerDiff(items)
            }
        })

        collectViewModel.gpsBreadcrumbs.observe(this, Observer { breadcrumbs ->
            mapFragment?.getMapAsync { map ->

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
            }
        })

        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            mapFragment = SupportMapFragment.newInstance(MapboxMapOptions().styleUrl("mapbox://styles/jesseblack/cjlwkyu3p3qjw2rpqtpxnsb5j"))
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.collectionMap, mapFragment, MAP_FRAGMENT_TAG)
                    .commit()

        } else {
            mapFragment = childFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG) as SupportMapFragment
        }

        mapFragment?.getMapAsync {
            markerManagerLiveData.postValue(MapboxItemMarkerManager(requireContext(), it))

            it.setOnMarkerClickListener(this@CollectFragment)
            it.addOnMapClickListener(this@CollectFragment)
        }

        LiveDataPair(markerManagerLiveData, collectViewModel.locationService.locationLiveData).observe(this, Observer {
            it?.let { (markerManager, locationUpdate) ->
                locationUpdate.let { (latLng, accuracy) ->
                    collectCardVm.currentLocation.set(latLng)
                    markerManager.setCurrentLocation(latLng.toMapboxLatLng(), accuracy.toDouble())
                    if (collectViewModel.lastKnownLocation == null) {
                        mapFragment?.getMapAsync {
                            it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng.toMapboxLatLng(), CollectViewModel.zoomLevel))
                        }
                    }

                    collectViewModel.lastKnownLocation = latLng.toMapboxLatLng()
                }
            }
        })

        mapFragment?.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        collectViewModel.lastKnownLocation?.let { lastLocation ->
            mapFragment?.getMapAsync {
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, CollectViewModel.zoomLevel))
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        markerManagerLiveData.value?.getCollectItem(marker)?.let { collectItem ->

            collectCardVm.visibility = true
            collectCardVm.itemData.set(collectItem)
        }

        return true
    }

    override fun onMapClick(point: com.mapbox.mapboxsdk.geometry.LatLng) {
        if (collectCardVm.visibility) {
            collectCardVm.visibility = false
        }
    }

    private fun showCollectAddScreen(isLandmark: Boolean) {
        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrame, CollectAddFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(CollectAddFragment.IS_LANDMARK, isLandmark)
                    }
                })
                .addToBackStack(CollectAddFragment::class.java.name)
                .commit()
    }

    companion object {
        const val MAP_FRAGMENT_TAG = "collectFragment.MapboxFragment"

        fun newInstance(): Fragment {
            return CollectFragment()
        }
    }
}
