package org.taskforce.episample.config.geography

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.taskforce.episample.databinding.ItemConfigFileBinding
import org.taskforce.episample.utils.inflater

class EnumerationFileAdapter: RecyclerView.Adapter<FileViewHolder>() {
    var data = mutableListOf<String>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
            FileViewHolder(ItemConfigFileBinding.inflate(parent.context.inflater))

    override fun getItemCount(): Int = when(data.size) {
        0 -> 1
        else -> data.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        if (position == 0 && data.isEmpty()) {
            holder.bind("None")
        } else {
            holder.bind(data[position])
        }
    }
}

class FileViewHolder(private val binding: ItemConfigFileBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(filename: String) {
        binding.vm = FileItemViewModel(filename)
    }
}