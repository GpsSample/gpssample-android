package org.taskforce.episample.utils

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.support.design.widget.TextInputLayout
import android.view.View
import android.view.View.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

@BindingAdapter("visibleOrGone")
fun View.setVisibleOrGone(show: Boolean) {
    visibility = if (show) VISIBLE else GONE
}

@BindingAdapter("visible")
fun View.setVisible(show: Boolean) {
    visibility = if (show) VISIBLE else INVISIBLE
}

@BindingAdapter("text")
fun TextView.setLangText(res: Int) {
    this.text = this.context.getLanguageManager().getString(res)
}

@BindingAdapter("text")
fun EditText.setLangText(res: Int) {
    this.setText(this.context.getLanguageManager().getString(res))
}

@BindingAdapter("error")
fun TextInputLayout.setLangError(error: String?) {
    this.error = error
}

@BindingAdapter("enabled")
fun View.setViewEnabled(enabled: Boolean) {
    this.isEnabled = enabled
}

@BindingAdapter("imageUrl", "placeholderRes", requireAll = false)
fun ImageView.loadImage(imageUrl: String?, placeholder: Drawable?) {
    if (imageUrl != null) {
        // TODO: Apply the rounded square transformation only when needed
        val picasso = Picasso.get().load(imageUrl)
        if (placeholder != null) {
            picasso.placeholder(placeholder)
        }
        picasso.into(this)
    }
    else {
        setImageDrawable(placeholder)
    }
}

@BindingAdapter("android:src")
fun ImageView.loadImage(resource: Int) {
    setImageResource(resource)
}

@BindingAdapter("android:typeface")
fun TextView.setTypeface(resource: Int) {
    setTypeface(null, resource)
}
