package org.taskforce.episample.injection

import dagger.Module
import dagger.Provides
import org.taskforce.episample.config.base.ConfigStorage
import org.taskforce.episample.config.base.LiveConfigStorage

@Module
class ConfigStorageModule {

    @Provides
    fun provideConfigStorage() : ConfigStorage = LiveConfigStorage()
}
