package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.core.language.LanguageDescription
import org.taskforce.episample.core.language.LanguageService
import org.taskforce.episample.databinding.ItemNavigationLanguageBinding
import org.taskforce.episample.help.HelpManager
import org.taskforce.episample.utils.inflater
import javax.inject.Inject

class NavigationToolbarViewModel(
        application: Application,
        titleResId: Int,
        titleSubject: String? = "") : AndroidViewModel(application) {

    @Inject
    lateinit var languageService: LanguageService

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }

    var languageSelectVisibility = MutableLiveData<Boolean>().apply { value = false }

    var title = languageService.getString(titleResId, titleSubject)

    override fun onCleared() {
        super.onCleared()
        languageService.cleanup()
    }

    val languageAdapter = LanguageAdapter(
            LanguageDescription(languageService.currentLanguage.value!!.id,
                    languageService.currentLanguage.value!!.name),
            languageService.getAvailableLanguages(),
            {
                languageService.updateCurrentLanguage(it)
                languageSelectVisibility.postValue(false)
            })
}

class LanguageAdapter(
        private val selectedLanguage: LanguageDescription,
        languages: List<LanguageDescription>,
        private val languageChanged: (LanguageDescription) -> Unit) :
        RecyclerView.Adapter<LanguageViewHolder>() {


    private var data: List<Pair<Boolean, LanguageDescription>> = listOf()
        set (value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        data = languages.map { language ->
            return@map Pair(language.id == selectedLanguage.id, language)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LanguageViewHolder(ItemNavigationLanguageBinding.inflate(parent.context.inflater), { customLanguage ->
                data = data.map {
                    return@map Pair(it.second == customLanguage, it.second)
                }
                languageChanged(customLanguage)
            })

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(data[position])
    }
}

class LanguageViewHolder(private val binding: ItemNavigationLanguageBinding?,
                         private val languageChanged: (LanguageDescription) -> Unit) :
        RecyclerView.ViewHolder(binding?.root) {
    fun bind(item: Pair<Boolean, LanguageDescription>) {
        binding?.vm = NavigationLanguageItemViewModel(item.first, item.second.name, {
            languageChanged(item.second)
        })
    }
}