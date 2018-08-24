package org.taskforce.episample.collection.managers

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.taskforce.episample.collection.models.GpsBreadcrumb
import org.taskforce.episample.study.managers.StudyManager

class GpsBreadcrumbManager(private val studyManager: StudyManager) {

    private val disposables = mutableMapOf<String, Disposable>()

    fun observeBreadcrumbs(tag: String, breadcrumbObservable: Observable<GpsBreadcrumb>) {
        disposables[tag] = breadcrumbObservable.subscribe {
            studyManager.addBreadcrumb(it)
        }
    }

    fun releaseObservable(tag: String) {
        disposables[tag]?.dispose()
    }
}