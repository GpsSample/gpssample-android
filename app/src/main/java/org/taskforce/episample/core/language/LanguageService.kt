package org.taskforce.episample.core.language

import android.arch.lifecycle.*
import android.content.Context
import android.content.res.Resources
import org.taskforce.episample.core.interfaces.CustomLanguage
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.toolbar.managers.LanguageManager


class LanguageDescription(val id: String,
                          val name: String)

interface LanguageService {
    fun cleanup()

    val enumerationSubject: EnumerationSubject
    val currentLanguage: LiveData<CustomLanguage>
    fun updateCurrentLanguage(languageDescription: LanguageDescription)
    fun getString(id: Int, vararg arguments: String?): LiveData<String>
    fun getQuantityString(id: Int, quantity: Int): LiveData<String>
    fun getAvailableLanguages(): List<LanguageDescription>
}

class LiveLanguageService(applicationContext: Context, override val enumerationSubject: EnumerationSubject, private val customLanguages: List<CustomLanguage>) : LanguageService {
    val resources: Resources = applicationContext.resources

    class LanguageServiceString(val resourceKey: String,
                                val arguments: List<String?>) {

        override fun equals(other: Any?): Boolean {
            (other as? LanguageServiceString)?.let {
                return resourceKey == it.resourceKey
                        && arguments == it.arguments
            }
            return false
        }

        override fun hashCode(): Int {
            return resourceKey.hashCode() + arguments.hashCode()
        }
    }

    class LanguageServiceQuantityString(val resourceKey: String,
                                        val quantity: Int) {

        override fun equals(other: Any?): Boolean {
            (other as? LanguageServiceQuantityString)?.let {
                return resourceKey == it.resourceKey
                        && quantity == it.quantity
            }
            return false
        }

        override fun hashCode(): Int {
            return resourceKey.hashCode() + quantity.hashCode()
        }
    }

    private val languageRepository = LanguageRepository.getLanguageRepository(applicationContext)

    override fun updateCurrentLanguage(languageDescription: LanguageDescription) {
        val customDescription = customLanguages.firstOrNull {
            it.id == languageDescription.id
        }?.let {
            return@let LanguageDescription(it.id, it.name)
        }

        val builtInDescription = languageRepository.builtInLanguages().firstOrNull {
            it.id == languageDescription.id
        }?.let {
            return@let LanguageDescription(it.id, it.name)
        } ?: run {
            return@run languageRepository.languagePreference.value!!.builtInLanguage
        }

        languageRepository.languagePreference.postValue(
                LanguageRepository.LanguagePreference(
                        customDescription,
                        builtInDescription
                )
        )
    }

    override val currentLanguage: LiveData<CustomLanguage> = Transformations.map(languageRepository.languagePreference, { languagePreference ->
        return@map customLanguages.firstOrNull {
            languagePreference.loadedLanguage?.id == it.id
        } ?: languageRepository.builtInLanguages().first {
            languagePreference.builtInLanguage.id == it.id
        }
    })

    private val liveDataMap = mutableMapOf<LanguageServiceString, MutableLiveData<String>>()
    private val liveQuantityDataMap = mutableMapOf<LanguageServiceQuantityString, MutableLiveData<String>>()
    private val currentLanguageObserver: Observer<CustomLanguage> = Observer { language ->
        liveDataMap.forEach { serviceString, liveData ->
            liveData.postValue(resolveString(serviceString, language))
        }
        liveQuantityDataMap.forEach { serviceString, liveData ->
            liveData.postValue(resolveString(serviceString, enumerationSubject, language))
        }
    }

    init {
        currentLanguage.observeForever(currentLanguageObserver)
    }

    override fun cleanup() {
        currentLanguage.removeObserver(currentLanguageObserver)
    }

    override fun getAvailableLanguages(): List<LanguageDescription> {
        return languageRepository.availableLanguages()
    }

    override fun getString(id: Int, vararg arguments: String?): LiveData<String> {
        if (id == undefinedStringResourceId) {
            return MutableLiveData<String>().apply { value = "" }
        }

        val transformedId = resources.getResourceName(id)
        val languageServiceString = LanguageServiceString(transformedId, arguments.toList())
        liveDataMap[languageServiceString]?.let { liveData ->
            return liveData
        }

        val newLiveData = MutableLiveData<String>().apply {
            value = resolveString(languageServiceString, currentLanguage.value)
        }

        liveDataMap[languageServiceString] = newLiveData
        return newLiveData
    }

    override fun getQuantityString(id: Int, quantity: Int): LiveData<String> {
        if (id == undefinedStringResourceId) {
            return MutableLiveData<String>().apply { value = "" }
        }

        val transformedId = resources.getResourceName(id)
        val languageServiceString = LanguageServiceQuantityString(transformedId,
                quantity)
        liveQuantityDataMap[languageServiceString]?.let { liveData ->
            return liveData
        }

        val newLiveData = MutableLiveData<String>().apply {
            value = resolveString(languageServiceString, enumerationSubject, currentLanguage.value)
        }

        liveQuantityDataMap[languageServiceString] = newLiveData
        return newLiveData
    }

    companion object {
        const val undefinedStringResourceId = LanguageManager.undefinedStringResourceId//-27182899

        private fun resolveString(languageServiceString: LanguageServiceString, currentLanguage: CustomLanguage?): String? {
            val arguments = languageServiceString.arguments.toTypedArray()

            return currentLanguage?.let { customLanguage ->
                val resourceName = languageServiceString.resourceKey
                return@let if (languageServiceString.arguments.isEmpty()) {
                    customLanguage.strings[resourceName]
                } else {
                    customLanguage.strings[resourceName]?.format(*arguments) ?: ""
                }
            }
        }

        private fun resolveString(languageServiceQuantityString: LanguageServiceQuantityString, enumerationSubject: EnumerationSubject, currentLanguage: CustomLanguage?): String? {
            return currentLanguage?.let { customLanguage ->
                val resourceName = languageServiceQuantityString.resourceKey
                val subjectSource = if (languageServiceQuantityString.quantity != 1) {
                    enumerationSubject.plural
                } else {
                    enumerationSubject.singular
                }
                return@let customLanguage.strings[resourceName]?.format(languageServiceQuantityString.quantity, subjectSource)
            }
        }
    }
}