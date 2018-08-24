package org.taskforce.episample.config.base

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class ConfigManagerException(message: String) : Exception(message)

class LiveConfigManager(private val configStorage: ConfigStorage) : ConfigManager {

    private var configs = mutableListOf<Config>()
        set(value) {
            field = value
            availableConfigsSubject.onNext(value)
        }

    override fun availableConfigsObservable(): Observable<List<Config>> =
            availableConfigsSubject as Observable<List<Config>>

    private val availableConfigsSubject = BehaviorSubject.create<List<Config>>()

    init {
        configs = mutableListOf()

        loadConfigsFromDisk().subscribe(
                {
                    configs = it.toMutableList()
                },
                {
                    Log.d("ERROR", "Unable to load configs")
                }
        )
    }

    override fun takenNames(): Set<String> = configs.map { it.name }.toSet()

    override fun addConfig(config: Config): Completable {
        return writeConfigToDisk(config)
                .doOnComplete {
                    configs.add(config)
                    availableConfigsSubject.onNext(configs)
                    Completable.complete()
                }
    }

    override fun duplicateConfig(config: Config): Completable {
        var newName = config.name

        while (
                takenNames().contains(newName)
                && newName.length <= Config.nameMaxChars
        ) {
            newName = "$newName-1"
        }

        if (newName.length > Config.nameMaxChars) {
            return Completable.fromCallable {
                // TODO move message creation out of service layer
                throw ConfigManagerException("Configuration cannot be duplicated because name is too long. Please edit the name and try again.")
            }
        }

        return configStorage.loadConfigsFromDisk()
                .flatMapCompletable {
                    val match = it.first { it.id == config.id }
                    match.name = newName
                    match.id = UUID.randomUUID().toString()
                    addConfig(match)
                }
    }

    override fun deleteConfig(config: Config): Completable {
        return configStorage.deleteConfigFromDisk(config)
                .doOnComplete {
                    configs.remove(config)
                    availableConfigsSubject.onNext(configs)
                    Completable.complete()
                }
    }

    override fun availableConfigSize(): Int {
        return configs.size
    }

    override fun findConfigById(id: String) =
            Single.create<Config> {
                val config = configs.firstOrNull {
                    it.id == id
                }
                val emitter = it
                config?.let {
                    emitter.onSuccess(config)
                }

                if (config == null) {
                    emitter.onError(ConfigManagerException("findConfigById: id not found"))
                }
            }!!

    private fun loadConfigsFromDisk() = configStorage.loadConfigsFromDisk()
    private fun writeConfigToDisk(config: Config) = configStorage.writeConfigToDisk(config)
}