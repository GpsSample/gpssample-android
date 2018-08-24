package org.taskforce.episample.config.landmark

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.taskforce.episample.databinding.ItemConfigLandmarkBinding
import org.taskforce.episample.fileImport.models.LandmarkType
import org.taskforce.episample.utils.inflater

class LandmarkAdapter(val landmarkUpdateListener: LandmarkAdapter.LandmarkUpdateCallback): RecyclerView.Adapter<LandmarkTypeViewHolder>(), Observer<List<LandmarkType>>,
                        LandmarkItemViewModel.LandmarkInteractionCallbacks{
    override fun editLandmark(landmarkType: LandmarkType) {
        landmarkUpdateListener.editLandmark(landmarkType)
    }

    override fun deleteLandmark(landmarkType: LandmarkType) {
        data.removeAll { it.landmarkItem.id == landmarkType.id }
        notifyDataSetChanged()
        landmarkUpdateListener.removeLandmark(landmarkType)
    }

    var data = mutableListOf<LandmarkItemViewModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LandmarkTypeViewHolder(ItemConfigLandmarkBinding.inflate(parent.context.inflater))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: LandmarkTypeViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: List<LandmarkType>) {
        data = t.map {
            LandmarkItemViewModel(it, this)
        }.toMutableList()
    }

    override fun onError(e: Throwable) {
    }

    interface LandmarkUpdateCallback {
        fun removeLandmark(landmarkType: LandmarkType)
        fun editLandmark(landmarkType: LandmarkType)
    }
}

class LandmarkTypeViewHolder(private val binding: ItemConfigLandmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(item: LandmarkItemViewModel) {
        binding.vm = item
    }
}