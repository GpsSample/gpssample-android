package org.taskforce.episample.navigation.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import org.taskforce.episample.BuildConfig
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.MapboxItemMarkerManager
import org.taskforce.episample.collection.ui.CollectAddFragment
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.databinding.FragmentNavigationPlanBinding
import org.taskforce.episample.utils.getCompatColor
import org.taskforce.episample.utils.toMapboxLatLng

class NavigationPlanFragment : Fragment(), MapboxMap.OnMarkerClickListener, MapboxMap.OnMapClickListener {

    private lateinit var navigationPlanViewModel: NavigationPlanViewModel
    private lateinit var navigationToolbarViewModel: NavigationToolbarViewModel
    private lateinit var navigationCardViewModel: NavigationCardViewModel

    var adapter: NavigationItemAdapter? = null

    private var lastKnownLocation: LatLng? = null

    private lateinit var navigationPlanId: String

    private var mapFragment: SupportMapFragment? = null
    private val markerManagerLiveData = MutableLiveData<MapboxItemMarkerManager>()

    private val mapPreferences: SharedPreferences
        get() = requireActivity().getSharedPreferences(MAP_PREFERENCE_NAMESPACE, Context.MODE_PRIVATE)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_ACCESS_TOKEN)

        navigationPlanId = arguments!!.getString(ARG_NAVIGATION_PLAN_ID)

        navigationPlanViewModel = ViewModelProviders.of(this@NavigationPlanFragment,
                NavigationPlanViewModelFactory(
                        requireActivity().application,
                        navigationPlanId,
                        {
                            showNavigationScreen()
                        },
                        {
                            showAddLocationScreen()
                        })).get(NavigationPlanViewModel::class.java)
        lifecycle.addObserver(navigationPlanViewModel.locationService)
        navigationCardViewModel = ViewModelProviders.of(this@NavigationPlanFragment,
                NavigationPlanCardViewModelFactory(
                        requireActivity().application,
                        navigationPlanViewModel.config.userSettings,
                        navigationPlanViewModel.locationService.locationLiveData,
                        requireContext().getCompatColor(R.color.colorError),
                        requireContext().getCompatColor(R.color.colorWarning),
                        requireContext().getCompatColor(R.color.gpsAcceptable)

                )).get(NavigationPlanCardViewModel::class.java)

        navigationToolbarViewModel = ViewModelProviders.of(this,
                NavigationToolbarViewModelFactory(requireActivity().application,
                        R.string.navigation_plan)).get(NavigationToolbarViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentNavigationPlanBinding>(inflater, R.layout.fragment_navigation_plan, container, false)
        setHasOptionsMenu(true)

        val toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        binding.vm = navigationPlanViewModel
        binding.cardVm = navigationCardViewModel
        binding.toolbarVm = navigationToolbarViewModel
        binding.setLifecycleOwner(this)

        adapter = NavigationItemAdapter(CollectIconFactory(requireContext().resources),
                navigationPlanViewModel.config.displaySettings)
        binding.collectList.adapter = adapter

        LiveDataPair(markerManagerLiveData, navigationPlanViewModel.navigationItems).observe(this, Observer {
            it?.let { (markerManager, items) ->
                val sortedItems = items?.sortedBy { it.navigationOrder }
                adapter?.data = sortedItems

                markerManager.addMarkerDiff(items)
            }
        })

        LiveDataPair(markerManagerLiveData, navigationPlanViewModel.landmarks).observe(this, Observer {
            it?.let { (markerManager, landmarks) ->
                markerManager.addMarkerDiff(landmarks)
            }
        })

        navigationPlanViewModel.breadcrumbs.observe(this, Observer { breadcrumbs ->
            mapFragment?.getMapAsync { map ->
                val breadCrumbPath = PolylineOptions()
                        .width(5.0F)
                breadcrumbs?.sortedBy { it.dateCreated }?.forEach { breadCrumbPath.add(it.location.toMapboxLatLng()) }
                map.addPolyline(breadCrumbPath)
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.navigation, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        marker.let {
            markerManagerLiveData.value?.getCollectItem(marker)?.let { collectItem ->
                navigationCardViewModel.visibility.postValue(true)
                navigationCardViewModel.itemData.postValue(collectItem)
            }
        }

        return true
    }

    override fun onMapClick(point: LatLng) {
        navigationCardViewModel.visibility.postValue(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            mapFragment = SupportMapFragment.newInstance(MapboxMapOptions().styleUrl(navigationPlanViewModel.config.mapboxStyle.urlString))
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.collectionMap, mapFragment, MAP_FRAGMENT_TAG)
                    .commit()

        } else {
            mapFragment = childFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG) as SupportMapFragment
        }

        mapFragment?.getMapAsync {
            markerManagerLiveData.postValue(MapboxItemMarkerManager(requireContext(), mapPreferences, it))
            it.setOnMarkerClickListener(this)
            it.addOnMapClickListener(this)
        }

        LiveDataPair(markerManagerLiveData, navigationPlanViewModel.locationService.locationLiveData).observe(this, Observer {
            it?.let { (markerManager, locationPair) ->
                locationPair.let { (location, accuracy) ->
                    markerManager.setCurrentLocation(location.toMapboxLatLng(), accuracy.toDouble())
                    if (lastKnownLocation == null) {
                        mapFragment?.getMapAsync {
                            it.moveCamera(CameraUpdateFactory.newLatLngZoom(location.toMapboxLatLng(), CollectViewModel.zoomLevel))
                        }
                    }
                    lastKnownLocation = location.toMapboxLatLng()
                }
            }
        })

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        toolbar?.setNavigationOnClickListener {
            if (fragmentManager?.backStackEntryCount ?: 0 > 0) {
                fragmentManager?.popBackStack()
            } else {
                requireActivity().finish()
            }
        }

        mapFragment?.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        markerManagerLiveData.observe(this, Observer {
            it?.let {
                it.applyLayerSettings()
            }
        })

        lastKnownLocation?.let { lastLocation ->
            mapFragment?.getMapAsync {
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, CollectViewModel.zoomLevel))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
//            R.id.action_help -> {
//            }
//            R.id.action_language -> {
//                navigationToolbarViewModel.languageSelectVisibility.postValue(true)
//            }
        }
        return true
    }

    private fun showNavigationScreen() {
        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, NavigationFragment.newInstance(navigationPlanId))
                .addToBackStack(NavigationFragment::class.java.name)
                .commit()
    }

    private fun showAddLocationScreen() {
        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, CollectAddFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(CollectAddFragment.IS_LANDMARK, true)
                    }
                })
                .addToBackStack(CollectAddFragment::class.java.name)
                .commit()
    }

    companion object {
        const val MAP_PREFERENCE_NAMESPACE = "SHARED_MAPBOX_LAYER_PREFERENCES"
        const val MAP_FRAGMENT_TAG = "navigationPlanFragment.MapboxFragment"
        private const val ARG_NAVIGATION_PLAN_ID = "ARG_NAVIGATION_PLAN_ID"

        fun newInstance(navigationPlanId: String): Fragment {
            val fragment = NavigationPlanFragment()
            val bundle = Bundle()
            bundle.putString(ARG_NAVIGATION_PLAN_ID, navigationPlanId)
            fragment.arguments = bundle
            return fragment
        }
    }
}