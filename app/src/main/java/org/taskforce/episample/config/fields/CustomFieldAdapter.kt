package org.taskforce.episample.config.fields

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.taskforce.episample.databinding.ItemCustomFieldBinding
import org.taskforce.episample.utils.inflater

class CustomFieldAdapter(
        val customFieldDisplayFactory: CustomFieldDisplayFactory,
        val automatic: String,
        val primaryLabel: String,
        val containsPii: String) : RecyclerView.Adapter<CustomFieldViewHolder>(), Observer<List<CustomField>> {

    var data = listOf<CustomField>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CustomFieldViewHolder(ItemCustomFieldBinding.inflate(parent.context.inflater))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: CustomFieldViewHolder, position: Int) {

        val description = when {
            data[position].isAutomatic -> automatic
            data[position].isPrimary -> primaryLabel
            else -> {
                customFieldDisplayFactory.buildDescription(data[position]) +
                        if (data[position].isPersonallyIdentifiableInformation) {
                            ", $containsPii"
                        } else {
                            ""
                        }
            }
        }

        holder.bind(CustomFieldItemViewModel(
                data[position].isAutomatic,
                customFieldDisplayFactory.buildDisplayName(data[position]),
                description,
                data[position].customKey)
        )
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: List<CustomField>) {
        data = t
        notifyDataSetChanged()
    }

    override fun onError(e: Throwable) {
    }
}

class CustomFieldViewHolder(
        private val binding: ItemCustomFieldBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(vm: CustomFieldItemViewModel) {
        binding.vm = vm
    }
}