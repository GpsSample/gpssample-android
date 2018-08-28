package org.taskforce.episample.collection.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_collect.*
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.CollectIconFactory
import org.taskforce.episample.collection.managers.CollectionItemMarkerManager
import org.taskforce.episample.collection.viewmodels.CollectCardViewModel
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.collection.viewmodels.CollectViewModelFactory
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.CollectItem
import org.taskforce.episample.databinding.FragmentCollectBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.getCompatColor
import org.taskforce.episample.utils.inflater
import javax.inject.Inject

class CollectFragment : Fragment(), GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    @Inject
    lateinit var languageManager: LanguageManager
    lateinit var languageService: LanguageService

    lateinit var locationClient: FusedLocationProviderClient
    lateinit var collectIconFactory: CollectIconFactory
    lateinit var markerManager: CollectionItemMarkerManager

    lateinit var collectViewModel: CollectViewModel

    var adapter: CollectItemAdapter? =  null
    lateinit var mapFragment: SupportMapFragment
    lateinit var breadcrumbPath: PolylineOptions
    lateinit var collectCardVm: CollectCardViewModel

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as EpiApplication).component.inject(this)

        languageService = LanguageService(languageManager)

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        collectIconFactory = CollectIconFactory(requireContext().resources)

        breadcrumbPath = PolylineOptions().clickable(false)


        val lastKnownLocationObservable = Observable.create<Pair<LatLng, Float>> { emitter ->
            locationClient.requestLocationUpdates(LocationRequest.create().apply {
                interval = 30000
                fastestInterval = 1000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult?) {
                    result?.lastLocation?.let {
                        emitter.onNext(Pair(LatLng(it.latitude, it.longitude), it.accuracy))
                    }
                }
            }, null)
        }

        collectViewModel = ViewModelProviders.of(this@CollectFragment,
                CollectViewModelFactory(
                        requireActivity().application,
                        languageService,
                        Single.create<GoogleMap> { single ->
                            mapFragment.getMapAsync {
                                single.onSuccess(it)
                            }
                        },
                        lastKnownLocationObservable,
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
                lastKnownLocationObservable,
                requireContext().getCompatColor(R.color.colorError),
                requireContext().getCompatColor(R.color.colorWarning),
                requireContext().getCompatColor(R.color.gpsAcceptable))

        mapFragment = SupportMapFragment()
        mapFragment.getMapAsync {
            markerManager = CollectionItemMarkerManager(collectIconFactory, it)

            it.isMyLocationEnabled = true
            it.mapType = GoogleMap.MAP_TYPE_SATELLITE
            it.setOnMarkerClickListener(this@CollectFragment)
            it.setOnMapClickListener(this@CollectFragment)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentCollectBinding.inflate(inflater.context.inflater).apply {
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.collectionMap, mapFragment)
                    .commit()

            vm = collectViewModel
            cardVm = collectCardVm
        }

        adapter = CollectItemAdapter(CollectIconFactory(requireContext().resources),
                languageService.getString(R.string.collect_incomplete),
                collectViewModel.config.displaySettings)
        binding.collectList.adapter = adapter

        collectViewModel.collectItems.observe(this, Observer {
            val items = it
            val sortedItems = items?.sortedByDescending { it.dateCreated } ?: emptyList()
            adapter?.data = sortedItems

            val titleText = languageService.getString(R.string.collect_title, "${items?.size ?: 0}")
            collectTitle.text = titleText

            this@CollectFragment.mapFragment.getMapAsync {
                markerManager.addMarkerDiff(items ?: emptyList())
            }
        })

        collectViewModel.gpsBreadcrumbs.observe(this, Observer { breadcrumbs ->
            this@CollectFragment.mapFragment.getMapAsync { map ->
                val breadCrumbPath = PolylineOptions().clickable(false).color(R.color.greyHighlights)
                breadcrumbs?.sortedBy { it.dateCreated }?.forEach { breadCrumbPath.add(it.location) }
                map.addPolyline(breadCrumbPath)
            }
        })

        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let {
            val collectItem = it.tag as CollectItem
            collectCardVm.visibility = true
            collectCardVm.itemData.set(collectItem)
        }

        return true
    }

    override fun onMapClick(marker: LatLng?) {
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
        fun newInstance(): Fragment {
            return CollectFragment()
        }
    }
}