package org.taskforce.episample.config.base

import android.databinding.BaseObservable
import android.databinding.Bindable
import io.reactivex.Observable
import org.taskforce.episample.db.config.Config
import org.taskforce.episample.utils.bindDelegate

class ConfigItemViewModel(
        configItemMenuObservable: Observable<List<ConfigItemMenuViewModel>>,
        private val openConfig: (Config) -> Unit,
        tempDescription: String,
        private val config: Config) : BaseObservable() {

    val title = config.name

    @get:Bindable
    var menuVisibility by bindDelegate(false)

    fun openConfig() {
        openConfig(config)
    }

    fun closeMenu() {
        menuVisibility = false
    }

    fun showMenu() {
        menuVisibility = true
    }

    val adapter = ConfigItemMenuAdapter(config)

    init {

        configItemMenuObservable.subscribe(adapter)
    }
}