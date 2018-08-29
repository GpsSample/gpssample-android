package org.taskforce.episample.navigation.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.Single
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.CollectionItemMarkerManager
import org.taskforce.episample.collection.ui.CollectAddFragment
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.databinding.FragmentNavigationBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import javax.inject.Inject

class NavigationFragment : Fragment() {

    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService

    lateinit var locationClient: FusedLocationProviderClient
    lateinit var collectIconFactory: CollectIconFactory
    lateinit var markerManager: CollectionItemMarkerManager

    lateinit var navigationViewModel: NavigationViewModel

    var adapter: NavigationItemAdapter? = null
    lateinit var mapFragment: SupportMapFragment

    private var googleMap: GoogleMap? = null
    private var lastKnownLocation: LatLng? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        languageService = LanguageService(languageManager)

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        collectIconFactory = CollectIconFactory(requireContext().resources)

        mapFragment = SupportMapFragment()

        val lastKnownLocationObservable = Observable.create<Pair<LatLng, Float>> { emitter ->
            locationClient.requestLocationUpdates(LocationRequest.create().apply {
                interval = 30000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            }, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult?) {
                    result?.lastLocation?.let {
                        emitter.onNext(Pair(LatLng(it.latitude, it.longitude), it.accuracy))
                    }
                }
            }, null)
        }

        lastKnownLocationObservable.subscribe {
            val location = it.first
            val accuracy = it.second

            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, CollectViewModel.zoomLevel))
            lastKnownLocation = location
        }

        Single.create<GoogleMap> { single ->
            mapFragment.getMapAsync {
                single.onSuccess(it)
            }
        }.subscribe( {
            googleMap = it
            lastKnownLocation?.let { location ->
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(location, CollectViewModel.zoomLevel))
            }

        },
                {
                    //TODO: Display unable to load Google Map.
                }
        )

        navigationViewModel = ViewModelProviders.of(this@NavigationFragment,
                NavigationViewModelFactory(
                        requireActivity().application,
                        languageService,
                        {
                            showAddLocationScreen()
                        },
                        {
                            requireActivity().supportFragmentManager.popBackStack()
                        })).get(NavigationViewModel::class.java)

        mapFragment.getMapAsync {
            markerManager = CollectionItemMarkerManager(collectIconFactory, it)

            it.isMyLocationEnabled = true
            it.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentNavigationBinding>(inflater, R.layout.fragment_navigation, container, false)

        childFragmentManager
                .beginTransaction()
                .replace(R.id.collectionMap, mapFragment)
                .commit()

        binding.vm = navigationViewModel

        adapter = NavigationItemAdapter(CollectIconFactory(requireContext().resources),
                navigationViewModel.config.displaySettings)
        binding.collectList.adapter = adapter

        navigationViewModel.enumerations.observe(this, Observer { items ->
            val sortedItems = items?.sortedByDescending { it.dateCreated } ?: emptyList()
            adapter?.data = sortedItems
        })
        navigationViewModel.collectItems.observe(this, Observer { items ->
            this@NavigationFragment.mapFragment.getMapAsync {
                markerManager.addMarkerDiff(items ?: emptyList())
            }
        })

        binding.setLifecycleOwner(this)

        return binding.root
    }

    private fun showAddLocationScreen() {
        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrame, CollectAddFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(CollectAddFragment.IS_LANDMARK, true)
                    }
                })
                .addToBackStack(CollectAddFragment::class.java.name)
                .commit()
    }

    companion object {
        fun newInstance(): Fragment {
            return NavigationFragment()
        }
    }
}