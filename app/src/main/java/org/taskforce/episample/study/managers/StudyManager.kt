package org.taskforce.episample.study.managers

import android.os.Environment
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.taskforce.episample.BuildConfig
import org.taskforce.episample.collection.models.CollectItem
import org.taskforce.episample.collection.models.EnumerationItem
import org.taskforce.episample.collection.models.GpsBreadcrumb
import org.taskforce.episample.collection.models.LandmarkItem
import org.taskforce.episample.config.base.Config
import org.taskforce.episample.config.base.ConfigManager
import org.taskforce.episample.study.models.Study

class StudyManager(private val configManager: ConfigManager,
                   private val studyStorage: StudyStorage) {

    fun verifyPassword(input: String?) =
            currentStudy?.password != null && input != null &&
                    currentStudy?.password == input

    val studyInProgress: Boolean
        get() = currentStudy != null

    var currentStudy: Study? = studyStorage.loadStudyFromDisk()

    private val collectionSubject = BehaviorSubject.create<List<CollectItem>>()

    val collectionObservable: Observable<List<CollectItem>>
        get() = collectionSubject as Observable<List<CollectItem>>

    val breadcrumbObservable: Observable<List<GpsBreadcrumb>> = PublishSubject.create<List<GpsBreadcrumb>>()

    val landmarkObservable: Observable<List<LandmarkItem>> = PublishSubject.create<List<LandmarkItem>>()

    val enumerationObservable: Observable<List<EnumerationItem>> = PublishSubject.create<List<EnumerationItem>>()

    fun createStudy(config: Config, name: String, password: String): Single<Boolean> {
        currentStudy = Study(name, password, config)
        return studyStorage.writeStudyToDisk(currentStudy)
    }

    fun addBreadcrumb(breadcrumb: GpsBreadcrumb) {
        currentStudy?.gpsBreadcrumbs?.add(breadcrumb)
        (breadcrumbObservable as PublishSubject<List<GpsBreadcrumb>>).onNext(currentStudy!!.gpsBreadcrumbs)
        collectionSubject.onNext(currentStudy?.collectItems ?: listOf())
    }

    fun addLandmark(landmarkItem: LandmarkItem) {
        currentStudy?.landmarks?.add(landmarkItem)
        (landmarkObservable as PublishSubject<List<LandmarkItem>>).onNext(currentStudy!!.landmarks)
        collectionSubject.onNext(currentStudy!!.collectItems)
    }

    fun addEnumerationPoint(enumerationItem: EnumerationItem) {
        currentStudy?.enumerationItems?.add(enumerationItem)
        (enumerationObservable as PublishSubject<List<EnumerationItem>>).onNext(currentStudy!!.enumerationItems)
        collectionSubject.onNext(currentStudy!!.collectItems)
    }

    companion object {
        internal val studyDirectory = "${Environment.getExternalStorageDirectory()}/${BuildConfig.FILE_DIRECTORY_NAME}/Study/"
        internal val studyExtension = ".study"
    }
}