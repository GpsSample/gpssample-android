package org.taskforce.episample.config.base

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ConfigManager {
    fun availableConfigsObservable(): Observable<List<Config>>
    fun addConfig(config: Config): Completable
    fun duplicateConfig(config: Config): Completable
    fun deleteConfig(config: Config): Completable
    fun findConfigById(id: String): Single<Config>

    fun availableConfigSize(): Int
    fun takenNames(): Set<String>
}
