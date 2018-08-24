package org.taskforce.episample.fileImport.processors

import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class LanguageProcessor {

    fun getLanguageFromFile(file: File, expectedCount: Int = 0): ProcessedLanguageResponse {

        val language = mutableMapOf<String, String>()
        val controls = mutableMapOf<String, Any?>()
        val elements = DocumentBuilderFactory
                .newInstance().newDocumentBuilder()
                .parse(file).getElementsByTagName(stringTag)

        for (i in 0..elements.length) {
            val element = (elements.item(i) as Element)
            val name = element.getAttribute(nameTag)
            when (name) {
                languageCode,
                languageName,
                isAdmin,
                isUser -> {
                    val text = element.textContent
                    if (text == trueTag) {
                        controls[name] = true
                    }
                    else if (text == falseTag) {
                        controls[name] = false
                    }
                }
                else -> {
                    language[name] = element.textContent
                }
            }
        }

        val valid = controls.contains(languageCode) &&
                controls.contains(languageName) &&
                controls.contains(isAdmin) &&
                controls.contains(isUser)

        return if (valid) {
            ProcessedLanguageResponse(valid, controls[isAdmin] as Boolean, controls[isUser] as Boolean, expectedCount - language.size)
        } else {
            ProcessedLanguageResponse(valid, false, false, -1)
        }
    }

    fun getLanguageFromTarget(target: String) =
            getLanguageFromFile(File(target))

    companion object {
        const val trueTag = "true"
        const val falseTag = "falseTag"
        const val stringTag = "string"
        const val nameTag = "name"
        const val languageCode = "language_code"
        const val languageName = "language_name"
        const val isAdmin = "is_admin"
        const val isUser = "is_user"
    }

}

data class ProcessedLanguageResponse(val isValid: Boolean,
                                     val hasAdmin: Boolean,
                                     val hasUser: Boolean,
                                     val missingCount: Int)