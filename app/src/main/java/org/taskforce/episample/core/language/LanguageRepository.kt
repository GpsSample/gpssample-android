package org.taskforce.episample.core.language

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import org.taskforce.episample.core.interfaces.BuiltInLanguage
import java.util.*

class LanguageRepository(applicationContext: Context) {

    /**
     * `loadedLanguage` comes from config, consumers of repository must use
     * fallback `builtInLanguage` when the loadedLanguage cannot be resolved
     */
    data class LanguagePreference(val loadedLanguage: LanguageDescription?,
                                  val builtInLanguage: LanguageDescription)

    fun availableLanguages(): List<LanguageDescription> {
        return builtInLanguages.map {
            LanguageDescription(it.id, it.name)
        }
    }

    fun builtInLanguages(): List<BuiltInLanguage> {
        return builtInLanguages
    }

    private val builtInLanguages: List<BuiltInLanguage>

    init {
        val importer = LanguageImporter(applicationContext)
        builtInLanguages = importer.getResourceLanguages(supportedLocales).map {
            it
        }
    }

    var languagePreference = MutableLiveData<LanguagePreference>().apply {
        value = LanguagePreference(null,
                LanguageDescription(builtInLanguages.first().id,
                        builtInLanguages.first().name))
    }

    companion object {
        val supportedLocales = listOf(
                Locale.forLanguageTag("en"),
                Locale.forLanguageTag("fr"),
                Locale.forLanguageTag("es")
        )

        private var INSTANCE: LanguageRepository? = null

        fun getLanguageRepository(applicationContext: Context): LanguageRepository {
            if (INSTANCE == null) {
                synchronized(LanguageRepository::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = LanguageRepository(applicationContext)
                    }
                }
            }
            return INSTANCE!!
        }
    }
}
