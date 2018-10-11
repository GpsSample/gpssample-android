package org.taskforce.episample.collection.ui

import android.databinding.BaseObservable
import android.text.SpannableStringBuilder

class CollectDetailFieldViewModel(val name: String,
                                  val value: SpannableStringBuilder,
                                  val showIcon: Boolean,
                                  val showText: Boolean,
                                  var image: String) : BaseObservable()
