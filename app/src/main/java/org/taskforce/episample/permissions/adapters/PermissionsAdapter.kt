package org.taskforce.episample.permissions.adapters

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.item_permission.view.*
import org.taskforce.episample.databinding.ItemPermissionBinding
import org.taskforce.episample.permissions.models.PermissionItem
import org.taskforce.episample.utils.inflater

class PermissionsAdapter : RecyclerView.Adapter<PermissionHolder>(), Observer<List<PermissionItem>>, LifecycleObserver {

    lateinit var disposable: Disposable

    private var data = listOf<PermissionItem>()
        set (value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PermissionHolder(ItemPermissionBinding.inflate(parent.context.inflater))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: PermissionHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
        disposable = d
    }

    override fun onNext(t: List<PermissionItem>) {
        data = t
    }

    override fun onError(e: Throwable) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposable.dispose()
    }
}

class PermissionHolder(private val binding: ItemPermissionBinding?) :
        RecyclerView.ViewHolder(binding?.root) {
    fun bind(item: PermissionItem) {
        itemView.permissionChecked.isChecked = item.hasPermission
        itemView.permissionTitle.text = item.title
        itemView.permissionText.text = item.text
    }
}