package org.taskforce.episample.toolbar.managers

import android.content.Context
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.taskforce.episample.config.language.CustomLanguage
import org.taskforce.episample.config.language.LanguageImporter
import java.util.*

class LanguageManager(private val applicationContext: Context) {

    val languages = mutableMapOf<String, CustomLanguage>()

    private val languageSubject = PublishSubject.create<LanguageEvent>()

    private val currentLanguagesSubject = BehaviorSubject.create<List<Pair<Boolean, CustomLanguage>>>()

    private val currentLanguages: List<Pair<Boolean, CustomLanguage>>
        get() {
            return languages.map {
                (it.key == currentLanguage) to it.value
            }
        }

    lateinit var currentLanguage: String

    val languageEventObservable: Observable<LanguageEvent>
        get() = languageSubject

    val currentLanguagesObservable: Observable<List<Pair<Boolean, CustomLanguage>>>
        get() = currentLanguagesSubject

    init {
        val importer = LanguageImporter(applicationContext)
        importer.getResourceLanguages(supportedLocales).filter {
            it.isValid
        }.map {
            it.customLanguage.id to it
        }.forEach {
            languages[it.first] = it.second.customLanguage
        }
        selectLanguage(importer.defaultLanguage.id)
    }

    fun addLanguage(language: CustomLanguage) {
        languages[language.id] = language
    }

    fun removeLanguage(langKey: String) {
        languages.remove(langKey)
    }

    fun selectLanguage(langKey: String) {
        languages[langKey]?.let {
            currentLanguage = it.id
            languageSubject.onNext(LanguageEvent.LANGUAGE_CHANGED)
            currentLanguagesSubject.onNext(currentLanguages)
        }
    }

    fun getString(id: Int, vararg arguments: String?): String {
        if (id == undefinedStringResourceId) { return "" }

        val foundString = languages[currentLanguage]!!.
                strings[applicationContext.resources.getResourceName(id)]!!
        return if (arguments.isNotEmpty()) {
            foundString.format(*arguments)
        }
        else {
            foundString
        }
    }

    companion object {

        const val undefinedStringResourceId = -27182899

        val supportedLocales = listOf(
                Locale.forLanguageTag("en"),
                Locale.forLanguageTag("fr"),
                Locale.forLanguageTag("es"))
    }
}

enum class LanguageEvent {
    LANGUAGE_CHANGED
}