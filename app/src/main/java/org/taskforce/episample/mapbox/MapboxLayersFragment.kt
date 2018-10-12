package org.taskforce.episample.mapbox

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import com.mapbox.mapboxsdk.style.layers.Property.NONE
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import kotlinx.android.synthetic.main.fragment_mapbox_layers.*
import kotlinx.android.synthetic.main.fragment_sync_with_enumerator.*
import org.taskforce.episample.BuildConfig
import org.taskforce.episample.R
import org.taskforce.episample.collection.managers.MapboxItemMarkerManager
import org.taskforce.episample.core.models.MapboxStyleUrl
import org.taskforce.episample.databinding.FragmentMapboxLayersBinding
import org.taskforce.episample.db.config.EnumerationSubject
import org.taskforce.episample.sync.ui.EnumeratorsAdapter
import org.taskforce.episample.toolbar.viewmodels.AppToolbarViewModel
import org.taskforce.episample.toolbar.viewmodels.AppToolbarViewModelFactory
import org.taskforce.episample.utils.latLngBounds

class MapboxLayersFragment : Fragment() {

    lateinit var viewModel: MapboxLayersViewModel

    private var mapFragment: SupportMapFragment? = null

    lateinit var toolbarViewModel: AppToolbarViewModel

    val preferenceNamespace: String
        get() = arguments!!.getString(ARG_PREFERENCE_NAMESPACE)
    val preferences: SharedPreferences
        get() = requireActivity().getSharedPreferences(preferenceNamespace, Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_ACCESS_TOKEN)

        viewModel = ViewModelProviders.of(requireActivity(), MapboxLayersViewModelFactory(
                requireActivity().application))
                .get(MapboxLayersViewModel::class.java)

        toolbarViewModel = ViewModelProviders.of(this,
                AppToolbarViewModelFactory(
                        requireActivity().application,
                        R.string.mapbox_layers_title,
                        // A config enumeration subject isn't created nor needed at this point
                        EnumerationSubject("any", "any", "any", "any")))
                .get(AppToolbarViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentMapboxLayersBinding>(inflater, R.layout.fragment_mapbox_layers, container, false)

        binding.vm = viewModel
        binding.toolbarVm = toolbarViewModel
        binding.setLifecycleOwner(this)

        val toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.studyConfig.observe(this, Observer { config ->
            if (savedInstanceState == null) {
                mapFragment = SupportMapFragment.newInstance(
                        MapboxMapOptions()
                                .styleUrl(config?.mapboxStyle?.urlString
                                        ?: MapboxStyleUrl.DEFAULT_MAPBOX_STYLE)
                )
                childFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_mapbox_layers_mapFrame, mapFragment, MapboxDownloadFragment.MAP_FRAGMENT_TAG)
                        .commit()

            } else {
                mapFragment = childFragmentManager.findFragmentByTag(MapboxDownloadFragment.MAP_FRAGMENT_TAG) as SupportMapFragment
            }

            val adapter = MapboxLayersAdapter(
                    { (name, isChecked) ->
                        mapFragment?.getMapAsync {
                            val matching = it.layers.filter { it.id == name }
                            val layerVisibility = if (isChecked) {
                                VISIBLE
                            } else {
                                NONE
                            }
                            matching.first().setProperties(visibility(layerVisibility))
                            with (preferences.edit()) {
                                putBoolean(name, isChecked)
                                commit()
                            }
                        }
                    })

            fragment_mapbox_layers_recyclerView.adapter = adapter
            fragment_mapbox_layers_recyclerView.layoutManager = LinearLayoutManager(requireActivity())

            val latLng: LatLng = arguments!!.getParcelable(ARG_LAT_LNG)
            val zoom = arguments!!.getDouble(ARG_MAP_ZOOM)

            mapFragment?.getMapAsync {
                MapboxItemMarkerManager(requireContext(), preferences, it).applyLayerSettings()

                it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
                val adapterLayers = it.layers.map {
                    val visibility = if (preferences.contains(it.id)) {
                        preferences.getBoolean(it.id, true)
                    } else {
                        it.visibility.value == VISIBLE
                    }

                    MapboxLayerSetting(it.id, visibility)
                }
                adapter.setLayers(adapterLayers)
            }

            val enumerationAreas = config?.enumerationAreas

            enumerationAreas?.let { enumerationAreas ->

                mapFragment?.getMapAsync {
                    it.moveCamera(CameraUpdateFactory.newLatLngBounds(enumerationAreas.latLngBounds(), 1))

                    val polygonOptions = enumerationAreas.map {
                        PolygonOptions()
                                .fillColor(R.color.enumerationAreaColor)
                                .addAll(it.points.map { latLng ->
                                    LatLng(latLng.lat, latLng.lng)
                                })
                    }
                    it.addPolygons(polygonOptions)
                }
            }
        })
    }

    companion object {
        private const val ARG_PREFERENCE_NAMESPACE = "ARG_PREFERENCE_NAMESPACE"
        private const val ARG_LAT_LNG = "LAT_LNG_BOUNDS"
        private const val ARG_MAP_ZOOM = "MAP_ZOOM"

        fun newInstance(latLng: LatLng, mapZoom: Double, preferenceNamespace: String): Fragment {
            val fragment = MapboxLayersFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_LAT_LNG, latLng)
                putDouble(ARG_MAP_ZOOM, mapZoom)
                putString(ARG_PREFERENCE_NAMESPACE, preferenceNamespace)
            }
            return fragment
        }
    }
}