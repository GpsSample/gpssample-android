package org.taskforce.episample.config.landmark

import android.content.Context
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.fileImport.models.LandmarkType
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.getResourceUri

class LandmarkTypeManager(
        private val context: Context,
        languageManager: LanguageManager) {

    val languageService = LanguageService(languageManager)

    val landmarks = mutableListOf<LandmarkType>()

    private val landmarkIcons = mutableListOf<String>()

    private val landmarksSubject = BehaviorSubject.create<List<LandmarkType>>()

    private val landmarkIconsSubject = BehaviorSubject.create<List<String>>()

    val landmarksObservable = landmarksSubject as Observable<List<LandmarkType>>

    val landmarksIconObservable = landmarkIconsSubject as Observable<List<String>>

    init {
        addLandmarkIcon(listOf(
                R.drawable.icon_landmark_default,
                R.drawable.apartments,
                R.drawable.barn_shed,
                R.drawable.bridge,
                R.drawable.bus_stop,
                R.drawable.hotel,
                R.drawable.market,
                R.drawable.medical,
                R.drawable.meeting_place,
                R.drawable.official,
                R.drawable.park,
                R.drawable.petrol,
                R.drawable.river_stream,
                R.drawable.school,
                R.drawable.shop,
                R.drawable.tree,
                R.drawable.water_source,
                R.drawable.worship
        ))
        addLandmarkType(
                mapOf(R.string.landmark_apartments to R.drawable.apartments,
                        R.string.landmark_barn_shed to R.drawable.barn_shed,
                        R.string.landmark_bridge to R.drawable.bridge,
                        R.string.landmark_bus_stop to R.drawable.bus_stop,
                        R.string.landmark_hotel to R.drawable.hotel,
                        R.string.landmark_market to R.drawable.market,
                        R.string.landmark_medical to R.drawable.medical,
                        R.string.landmark_meeting_place to R.drawable.meeting_place,
                        R.string.landmark_official to R.drawable.official,
                        R.string.landmark_park to R.drawable.park,
                        R.string.landmark_petrol to R.drawable.petrol,
                        R.string.landmark_river_stream to R.drawable.school,
                        R.string.landmark_school to R.drawable.school,
                        R.string.landmark_shop to R.drawable.shop,
                        R.string.landmark_tree to R.drawable.tree,
                        R.string.landmark_water_source to R.drawable.water_source,
                        R.string.landmark_worship to R.drawable.worship
                ).toList().map {
                    LandmarkType(languageService.getString(it.first), findIconByResourceId(it.second))
                }
        )
    }

    fun addLandmarkIcon(location: String) {
        landmarkIcons.add(location)
        landmarkIconsSubject.onNext(landmarkIcons)
    }

    private fun findIconByResourceId(resource: Int) =
            landmarkIcons.firstOrNull {
                it.split("/".toRegex()).last() == resource.toString()
            }

    private fun addLandmarkIcon(resource: Int) {
        addLandmarkIcon(context.resources.getResourceUri(resource).toString())
    }

    private fun addLandmarkIcon(resources: List<Int>) {
        resources.forEach {
            addLandmarkIcon(it)
        }
    }

    fun addLandmarkType(type: LandmarkType) {
        landmarks.add(type)
        landmarksSubject.onNext(landmarks)
    }

    fun editLandmarkType(type: LandmarkType) {
        val index = landmarks.indexOfFirst { it.id == type.id }
        if (index >= 0) {
            landmarks[index] = type
            landmarksSubject.onNext(landmarks)
        }
    }

    fun removeLandmarkType(type: LandmarkType) {
        landmarks.remove(type)
        landmarksSubject.onNext(landmarks)
    }

    private fun addLandmarkType(types: List<LandmarkType>) {
        types.forEach {
            addLandmarkType(it)
        }
    }
}