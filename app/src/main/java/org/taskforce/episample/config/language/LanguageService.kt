package org.taskforce.episample.config.language

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import org.taskforce.episample.toolbar.managers.LanguageManager

class LanguageService(
        private val languageManager: LanguageManager) : LifecycleObserver {

    var update: ((LanguageService) -> Unit)? = null

    private val languageEventDisposable =
            languageManager.languageEventObservable.subscribe {
        update?.invoke(this)
    }

    fun getString(res: Int, vararg arguments: String?) = languageManager.getString(res, *arguments)

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        languageEventDisposable.dispose()
    }
}
