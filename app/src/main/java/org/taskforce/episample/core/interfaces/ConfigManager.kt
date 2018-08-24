package org.taskforce.episample.core.interfaces

import android.arch.lifecycle.LiveData

interface ConfigManager {
    val configId: String
    fun getConfig(): LiveData<Config>
}