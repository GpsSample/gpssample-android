package org.taskforce.episample.config.base

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.taskforce.episample.databinding.ItemConfigBinding
import org.taskforce.episample.db.config.Config
import org.taskforce.episample.utils.inflater
class ConfigAllAdapter(
        private val configItemMenuObservable: Observable<List<ConfigItemMenuViewModel>>,
        private val descriptionText: String,
        private val openConfig: (Config) -> Unit)
    : RecyclerView.Adapter<ConfigAllViewHolder>(), Observer<List<Config>> {

    private var data = listOf<Config>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ConfigAllViewHolder(ItemConfigBinding.inflate(parent.context.inflater))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ConfigAllViewHolder, position: Int) {
        holder.bind(ConfigItemViewModel(configItemMenuObservable, openConfig, descriptionText, data[position]))
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: List<Config>) {
        data = t
    }

    override fun onError(e: Throwable) {
    }

    fun setConfigs(configs: List<Config>) {
        data = configs
    }
}

class ConfigAllViewHolder(val binding: ItemConfigBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(vm: ConfigItemViewModel) {
        binding.vm = vm
    }
}