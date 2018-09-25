package org.taskforce.episample.core.interfaces

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.config.CommonManager

interface NavigationManager {
    val userSession: UserSession

    fun getLandmarkTypes(): List<LandmarkType>

    fun getNavigationPlan(navigationPlanId: String): LiveData<NavigationPlan>
    fun getNavigationItems(navigationPlanId: String): LiveData<List<NavigationItem>>
    fun getLandmarks(): LiveData<List<Landmark>>
    fun getCollectItems(): LiveData<List<CollectItem>>
    fun getBreadcrumbs(): LiveData<List<Breadcrumb>>
    fun updateSurveyStatus(navigationItemId: String, surveyStatus: SurveyStatus, callback: () -> Unit)

    // TODO add possible path
//    fun getPossiblePath(): LiveData<List<Breadcrumb>>
    // TODO add navigation breadcrumbs to db (flag indicating breadcrumb recording phase)
//    fun addBreadcrumb(breadcrumb: Breadcrumb, callback: (breadcrumbId: String) -> Unit)
}

class LiveNavigationManager(val application: Application,
                            val config: Config,
                            val studyRepository: StudyRepository,
                            override val userSession: UserSession) : NavigationManager {

    val studyId: String
        get() = userSession.studyId
    val configId: String
        get() = userSession.configId

    override fun getNavigationPlan(navigationPlanId: String): LiveData<NavigationPlan> {
        return Transformations.map(studyRepository.getNavigationPlan(navigationPlanId)) { plan ->
            return@map LiveNavigationPlan(
                    plan.studyId,
                    plan.title,
                    plan.id,
                    plan.navigationItems.map { item ->
                        return@map LiveNavigationItem(
                                userSession.username,
                                item.enumeration.title ?: "",
                                item.navigationOrder,
                                item.surveyStatus,
                                item.enumeration.isIncomplete,
                                item.enumeration.customFieldValues,
                                item.enumeration.isExcluded,
                                item.id,
                                item.enumeration.note,
                                item.enumeration.image,
                                LatLng(item.enumeration.lat, item.enumeration.lng),
                                item.enumeration.gpsPrecision,
                                item.dateCreated
                        )
                    }
            )
        }
    }

    override fun getNavigationItems(navigationPlanId: String): LiveData<List<NavigationItem>> {
        return Transformations.map(getNavigationPlan(navigationPlanId)) {
            return@map it.navigationItems
        }
    }

    override fun getLandmarks(): LiveData<List<Landmark>> {
        return CommonManager.getLandmarks(config, studyRepository, studyId)
    }

    override fun getCollectItems(): LiveData<List<CollectItem>> {
        return CommonManager.getCollectItems(config, studyRepository, studyId)
    }

    override fun getBreadcrumbs(): LiveData<List<Breadcrumb>> {
        return CommonManager.getBreadcrumbs(studyRepository, studyId)
    }

    override fun getLandmarkTypes(): List<LandmarkType> {
        return config.landmarkTypes
    }

    override fun updateSurveyStatus(navigationItemId: String, surveyStatus: SurveyStatus, callback: () -> Unit) {
        studyRepository.updateNavigationItem(navigationItemId, surveyStatus, callback)
    }
}