package org.taskforce.episample.collection.viewmodels

import android.databinding.BaseObservable
import org.taskforce.episample.config.fields.CustomField

abstract class AbstractCustomViewModel(val customField: CustomField): BaseObservable() {

    abstract val value: Any?

}