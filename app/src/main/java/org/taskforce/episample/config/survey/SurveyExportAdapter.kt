package org.taskforce.episample.config.survey

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.item_config_survey_export.view.*
import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.config.fields.CustomFieldDisplayFactory
import org.taskforce.episample.databinding.ItemConfigSurveyExportBinding
import org.taskforce.episample.utils.inflater

class SurveyExportAdapter(
        val customFieldDisplayFactory: CustomFieldDisplayFactory,
        var piiWarning: String) : RecyclerView.Adapter<SurveyExportViewHolder>(), Observer<List<CustomField>> {

    private var data = mutableListOf<Pair<CustomField, Boolean>>()

    val selectedData: Map<CustomField, Boolean>
        get() = data.toMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SurveyExportViewHolder(
                    customFieldDisplayFactory,
                    ItemConfigSurveyExportBinding.inflate(parent.context.inflater),
                    data,
                    piiWarning)

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: SurveyExportViewHolder, position: Int) {
        holder.bind(data[position], position)
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: List<CustomField>) {
        data = t.map {
            it to true
        }.toMutableList()
    }

    override fun onError(e: Throwable) {
    }
}

class SurveyExportViewHolder(
        private val customFieldDisplayFactory: CustomFieldDisplayFactory,
        private val binding: ItemConfigSurveyExportBinding,
        private val data: MutableList<Pair<CustomField, Boolean>>,
        private val piiWarning: String) : RecyclerView.ViewHolder(binding.root) {

    fun bind(customField: Pair<CustomField, Boolean>, position: Int) {
        binding.vm = SurveyExportItemViewModel(
                customFieldDisplayFactory.buildDisplayName(customField.first),
                customFieldDisplayFactory.buildDescription(customField.first),
                piiWarning,
                customField.first.isPersonallyIdentifiableInformation
        )

        binding.root.customFieldExportChecked.setOnCheckedChangeListener { _, isChecked ->
            data[position] = Pair(customField.first, isChecked)
        }
    }
}