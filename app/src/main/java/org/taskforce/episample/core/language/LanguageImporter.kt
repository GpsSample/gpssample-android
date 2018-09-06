package org.taskforce.episample.core.language

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.BuiltInLanguage
import org.taskforce.episample.core.interfaces.CustomLanguage
import org.taskforce.episample.core.interfaces.LiveBuiltInLanguage
import java.util.*

class LanguageImporter(private val applicationContext: Context) {

    private val defaultLanguage: CustomLanguage

    init {
        defaultLanguage = generateLanguageFromResource(applicationContext.resources)
    }

    fun getResourceLanguages(locales: List<Locale>) =
            locales.map {
                applicationContext.createConfigurationContext(Configuration(applicationContext.resources.configuration)
                        .apply { setLocale(it) }).resources
            }.map {
                return@map generateLanguageFromResource(it)
            }

    private fun generateLanguageFromResource(res: Resources): BuiltInLanguage {
        val id: String
        val name: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            id = res.configuration.locales[0].isO3Language
            name = res.configuration.locales[0].getDisplayLanguage(res.configuration.locales[0]).capitalize()
        } else {
            id = res.configuration.locale.isO3Language
            name = res.configuration.locale.getDisplayLanguage(res.configuration.locale).capitalize()
        }
        return LiveBuiltInLanguage(
                id,
                name,
                R.string::class.java.fields.map {
                    res.getResourceName(it.getInt(null)) to
                            res.getString(it.getInt(null))
                }.toMap()
        )
    }
}