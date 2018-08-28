package org.taskforce.episample.core.interfaces

import android.arch.lifecycle.LiveData
import android.content.Context

interface ConfigManager {
    val configId: String
    fun getConfig(context: Context): LiveData<Config>
}