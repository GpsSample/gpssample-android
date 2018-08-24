package org.taskforce.episample.config.fields

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.taskforce.episample.databinding.ItemConfigFieldAddDropdownBinding
import org.taskforce.episample.utils.inflater

class CustomFieldsAddDropdownAdapter : RecyclerView.Adapter<DropdownViewHolder>() {

    var dropdownSize = 1

    private val viewModels = mutableSetOf<CustomFieldsAddDropdownViewModel>()

    val dropdownItems: List<CustomDropdown>
        get() {
            return viewModels.map {
                CustomDropdown(it.input)
            }
        }

    private val dropdownInputObservers = mutableListOf<Observable<String?>>()

    val dropdownInputObservableObservable: Observable<Observable<String?>> = BehaviorSubject.create<Observable<String?>>()

    private val dropdownInputObservable: Observable<String?>
        get() = Observable.merge(dropdownInputObservers)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DropdownViewHolder(ItemConfigFieldAddDropdownBinding.inflate(parent.context.inflater))

    override fun getItemCount() = dropdownSize

    override fun onBindViewHolder(holder: DropdownViewHolder, position: Int) {
        CustomFieldsAddDropdownViewModel((position + 1).toString(), "Option ${position + 1}").apply {
            holder.bind(this)
            viewModels.add(this)
            dropdownInputObservers.add(this.inputObservable)
            (dropdownInputObservableObservable as BehaviorSubject<Observable<String?>>).onNext(dropdownInputObservable)
        }
    }

    override fun onViewRecycled(holder: DropdownViewHolder) {
        super.onViewRecycled(holder)
        viewModels.remove(holder.viewModel)
        dropdownInputObservers.remove(holder.viewModel.inputObservable)
        (dropdownInputObservableObservable as BehaviorSubject<Observable<String?>>).onNext(dropdownInputObservable)
    }
}


class DropdownViewHolder(val binding: ItemConfigFieldAddDropdownBinding) :
        RecyclerView.ViewHolder(binding.root) {

    lateinit var viewModel: CustomFieldsAddDropdownViewModel

    fun bind(vm: CustomFieldsAddDropdownViewModel) {
        binding.vm = vm
        viewModel = vm

    }
}