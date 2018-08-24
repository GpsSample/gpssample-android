package org.taskforce.episample.permissions.viewmodels

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.databinding.BaseObservable
import android.databinding.Bindable
import io.reactivex.disposables.Disposable
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.permissions.adapters.PermissionsAdapter
import org.taskforce.episample.permissions.managers.PermissionManager
import org.taskforce.episample.utils.bindDelegate

class PermissionsViewModel(
        languageService: LanguageService,
        private val lifecycle: Lifecycle,
        private val permissionManager: PermissionManager,
        private val exit: () -> Unit) : BaseObservable(), LifecycleObserver {

    init {
        languageService.update = {
            buttonText = languageService.getString(R.string.allow_all)
        }
    }

    val permissionsAdapter = PermissionsAdapter()

    val disposables = mutableListOf<Disposable>()

    @get:Bindable
    var buttonText by bindDelegate(languageService.getString(R.string.allow_all))

    init {
        lifecycle.addObserver(this)
        lifecycle.addObserver(permissionsAdapter)
        permissionManager.permissionsObservable.subscribe(permissionsAdapter)
        disposables.add(permissionManager.permissionsObservable.subscribe {
            buttonText = if (permissionManager.allPermissions) {
                languageService.getString(R.string.allow_continue)
            } else {
                languageService.getString(R.string.allow_all)
            }
        })
    }

    fun next() {
        if (permissionManager.allPermissions) {
            exit()
        } else {
            permissionManager.requestPermissions()
        }
    }

    fun onRequestPermissionsResult() {
        permissionManager.onNext(Any())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposables.forEach {
            it.dispose()
        }
    }

    companion object {
        const val HELP_TARGET = "#permissions"
    }
}