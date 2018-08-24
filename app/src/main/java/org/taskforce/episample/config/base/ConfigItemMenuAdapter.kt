package org.taskforce.episample.config.base

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.taskforce.episample.databinding.ItemConfigMenuBinding
import org.taskforce.episample.db.config.Config
import org.taskforce.episample.utils.inflater

class ConfigItemMenuAdapter(private val config: Config) : RecyclerView.Adapter<ConfigItemMenuViewHolder>(), Observer<List<ConfigItemMenuViewModel>> {

    var data = listOf<ConfigItemMenuViewModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ConfigItemMenuViewHolder(ItemConfigMenuBinding.inflate(parent.context.inflater), config)

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ConfigItemMenuViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: List<ConfigItemMenuViewModel>) {
        data = t
    }

    override fun onError(e: Throwable) {
    }
}


class ConfigItemMenuViewHolder(private val binding: ItemConfigMenuBinding,
                               private val config: Config) : RecyclerView.ViewHolder(binding.root) {
    fun bind(viewModel: ConfigItemMenuViewModel) {
        binding.vm = viewModel
        binding.config = config
    }
}