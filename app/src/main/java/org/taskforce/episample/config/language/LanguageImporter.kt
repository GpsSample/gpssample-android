package org.taskforce.episample.config.language

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import org.taskforce.episample.R
import java.util.*

class LanguageImporter(private val applicationContext: Context) {

    val defaultLanguage: CustomLanguage

    init {
        defaultLanguage = generateLanguageFromResource(applicationContext.resources)
    }

    fun getResourceLanguages(locales: List<Locale>) =
            locales.map {
                applicationContext.createConfigurationContext(Configuration(applicationContext.resources.configuration)
                        .apply { setLocale(it) }).resources
            }.map {
                ValidatedCustomLanguage.build(defaultLanguage, generateLanguageFromResource(it))
            }

    private fun generateLanguageFromResource(res: Resources): CustomLanguage {
        val id: String
        val name: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            id = res.configuration.locales[0].isO3Language
            name = res.configuration.locales[0].getDisplayLanguage(res.configuration.locales[0]).capitalize()
        } else {
            id = res.configuration.locale.isO3Language
            name = res.configuration.locale.getDisplayLanguage(res.configuration.locale).capitalize()
        }
        return CustomLanguage(
                id,
                name,
                R.string::class.java.fields.map {
                    res.getResourceName(it.getInt(null)) to
                            res.getString(it.getInt(null))
                }.toMap()
        )
    }
}

data class ValidatedCustomLanguage(val customLanguage: CustomLanguage,
                                   val isValid: Boolean,
                                   val missingCount: Int) {
    companion object {
        fun build(validationLanguage: CustomLanguage, customLanguage: CustomLanguage): ValidatedCustomLanguage {
            val countMissing = countMissing(validationLanguage, customLanguage)
            return ValidatedCustomLanguage(customLanguage, countMissing == 0, countMissing)
        }

        private fun countMissing(validationLanguage: CustomLanguage, language: CustomLanguage) =
                validationLanguage.strings.map {
                    if (!language.strings.containsKey(it.key)) {
                        1
                    } else {
                        0
                    }
                }.reduce { acc, i -> acc + i }
    }
}