package org.taskforce.episample.config.landmark

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.taskforce.episample.databinding.ItemConfigAddLandmarkBinding
import org.taskforce.episample.utils.inflater

class LandmarkAddAdapter(private val context: Context) : RecyclerView.Adapter<LandmarkIconViewHolder>(), Observer<List<String>> {

    var preSelectedIcon: String? = null

    var data = listOf<LandmarkItemAddViewModel>()
        set(value) {
            field = value
            value.firstOrNull {
                it.isSelected
            }?.iconUrl?.let {
                selectedSubject.onNext(it)
            }
        }

    private val selectedSubject = PublishSubject.create<String>()

    val selectedObservable = selectedSubject as Observable<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LandmarkIconViewHolder(ItemConfigAddLandmarkBinding.inflate(parent.context.inflater))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: LandmarkIconViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: List<String>) {
        data = t.map {
            val selected = when(preSelectedIcon) {
                null -> false
                else -> !preSelectedIcon.isNullOrEmpty() && it == preSelectedIcon
            }
            LandmarkItemAddViewModel(context, this, selected, it)
        }
    }

    override fun onError(e: Throwable) {
    }
}

class LandmarkIconViewHolder(private val binding: ItemConfigAddLandmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(item: LandmarkItemAddViewModel) {
        binding.vm = item
    }
}