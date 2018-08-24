package org.taskforce.episample.config.landmark

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import org.taskforce.episample.R
import org.taskforce.episample.utils.getCompatColor

class LandmarkItemAddViewModel(
        val context: Context,
        val adapter: LandmarkAddAdapter,
        var isSelected: Boolean,
        val iconUrl: String?) {

    val background: Drawable
        get() = if (isSelected) {
            context.getDrawable(R.drawable.landmark_add_selected_background)
        } else {
            ColorDrawable(context.getCompatColor(android.R.color.white))
        }

    fun selected() {
        adapter.data = adapter.data.map {
            it.isSelected = it == this
            it
        }
        adapter.notifyDataSetChanged()
    }
}