package org.taskforce.episample.permissions.managers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.taskforce.episample.R
import org.taskforce.episample.permissions.models.PermissionItem
import org.taskforce.episample.toolbar.managers.LanguageManager

class PermissionManager(
        private val context: Context,
        private val languageManager: LanguageManager) : Observer<Any> {

    lateinit var permissionsRequest: (Array<String>) -> Unit

    private val permissions = mutableMapOf<Collection<String>, PermissionItem>()

    val permissionsObservable: Observable<List<PermissionItem>>
        get() = permissionsSubject

    val allPermissions: Boolean
        get() = checkAllPermissions()

    private val permissionsSubject: BehaviorSubject<List<PermissionItem>> = BehaviorSubject.create()

    init {
        languageManager.languageEventObservable.subscribe(this)
        buildPermissions()
        emitPermissions()
    }

    private fun retrievePermissions(): Map<String, Boolean> =
            permissions.keys.flatMap {
                it
            }.map {
                        it to (ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED)
                    }.toMap()

    private fun checkAllPermissions() =
            retrievePermissions().map {
                it.value
            }.reduce { composition, next ->
                        composition && next
                    }

    private fun emitPermissions() {
        val currentPermissions = retrievePermissions()
        permissions.forEach {
            it.value.hasPermission =
                    it.key.map {
                        currentPermissions[it] == true
                    }.reduce { composition, next ->
                                composition && next
                            }
        }
        permissionsSubject.onNext(permissions.values.toList())
    }

    private fun buildPermissions() {
        permissions[
                listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)] =
                PermissionItem(false,
                        languageManager.getString(R.string.permission_title_location),
                        languageManager.getString(R.string.permission_description_location))

        permissions[listOf(Manifest.permission.CAMERA)] =
                PermissionItem(false,
                        languageManager.getString(R.string.permission_title_camera),
                        languageManager.getString(R.string.permission_description_camera))

        permissions[listOf(Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)] =
                PermissionItem(false,
                        languageManager.getString(R.string.permission_title_wifi),
                        languageManager.getString(R.string.permission_description_wifi))
    }

    fun requestPermissions() {
        permissionsRequest(retrievePermissions().filter {
            !it.value
        }.keys.toTypedArray())
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: Any) {
        buildPermissions()
        emitPermissions()
    }

    override fun onError(e: Throwable) {
    }

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 9001
    }
}