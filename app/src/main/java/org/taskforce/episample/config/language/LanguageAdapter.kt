package org.taskforce.episample.config.language

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.item_config_language.view.*
import org.taskforce.episample.R
import org.taskforce.episample.config.base.Stepper
import org.taskforce.episample.databinding.ItemConfigLanguageBinding
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.inflater

class LanguageAdapter(private val stepper: Stepper,
                      private val languageManager: LanguageManager) :
        RecyclerView.Adapter<LanguageHolder>(), Observer<List<Pair<Boolean, CustomLanguage>>> {

    private var data = listOf<LanguageItemViewModel>()
        set (value) {
            field = value
            updateData(field)
        }

    val checkedLanguages
        get() = data
                .filter {
                    it.checked
                }
                .map {
                    it.customLanguage
                }

    private val dataSubject = BehaviorSubject.create<List<Pair<Boolean, String>>>()

    val dataObservable
        get() = dataSubject as Observable<List<Pair<Boolean, String>>>

    val anyChecked
        get() = data.isNotEmpty() && data.map { it.checked }.reduce { acc, value -> acc || value }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LanguageHolder(ItemConfigLanguageBinding.inflate(parent.context.inflater))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: LanguageHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: List<Pair<Boolean, CustomLanguage>>) {
        data = t.map {
            LanguageItemViewModel(
                    it.first,
                    it.second,
                    languageManager.getString(R.string.config_lang_admin),
                    languageManager.getString(R.string.config_lang_user), {
                updateData(data)
                stepper.enableNext(anyChecked, LanguageFragment::class.java)
            })
        }
    }

    override fun onError(e: Throwable) {
    }

    private fun updateData(field: List<LanguageItemViewModel>) {
        dataSubject.onNext(field.map {
            it.checked to it.customLanguage.name
        })
        notifyDataSetChanged()
    }
}

class LanguageHolder(private val binding: ItemConfigLanguageBinding?) : RecyclerView.ViewHolder(binding?.root) {
    fun bind(item: LanguageItemViewModel) {
        binding?.vm = item
        itemView.languageChecked.isChecked = item.checked
    }
}