package org.taskforce.episample.mock

import dagger.Module
import dagger.Provides
import org.mockito.Mockito
import org.taskforce.episample.config.base.Config
import org.taskforce.episample.config.base.ConfigStorage
import javax.inject.Singleton

@Module
class MockConfigStorageModule(val configStorage: ConfigStorage) {

    @Provides
    internal fun provideConfigStorage(): ConfigStorage {
        return configStorage
    }
}