package org.taskforce.episample.toolbar.ui

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.taskforce.episample.databinding.ItemLanguageBinding
import org.taskforce.episample.config.language.CustomLanguage
import org.taskforce.episample.toolbar.viewmodels.LanguageItemViewModel
import org.taskforce.episample.utils.inflater

class LanguageAdapter(private val languageChanger: (String) -> Unit) :
        RecyclerView.Adapter<LanguageViewHolder>(), Observer<List<Pair<Boolean, CustomLanguage>>> {

    lateinit var disposable: Disposable

    private var data = listOf<Pair<Boolean, CustomLanguage>>()
        set (value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LanguageViewHolder(ItemLanguageBinding.inflate(parent.context.inflater), languageChanger)

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
        disposable = d
    }

    override fun onNext(t: List<Pair<Boolean, CustomLanguage>>) {
        data = t
    }

    override fun onError(e: Throwable) {
    }
}

class LanguageViewHolder(private val binding: ItemLanguageBinding?,
                         private val languageChanger: (String) -> Unit) :
        RecyclerView.ViewHolder(binding?.root) {
    fun bind(item: Pair<Boolean, CustomLanguage>) {
        binding?.vm = LanguageItemViewModel(item.first, item.second.name, {
            languageChanger(item.second.id)
        })
    }
}