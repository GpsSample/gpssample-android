package org.taskforce.episample.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan

fun humanReadableBytes(bytes: Long): String {
    if (bytes < 1024) {
        return "${bytes}B"
    }
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), "KMGT"[exp - 1])
}

fun boldSubstring(target: String, substring: String): SpannableStringBuilder {
    val boldIndex = target.indexOf(substring)
    return SpannableStringBuilder(target).apply {
        setSpan(StyleSpan(Typeface.BOLD), boldIndex, boldIndex + substring.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}