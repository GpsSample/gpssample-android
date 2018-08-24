package org.taskforce.episample.config.fields

import android.databinding.BaseObservable
import android.databinding.Bindable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.taskforce.episample.utils.bindDelegate

class CustomFieldsAddDropdownViewModel(val number: String,
                                       val hint: String): BaseObservable() {

    @get:Bindable
    var input by bindDelegate<String?>(null, { _, newValue ->
        (inputObservable as BehaviorSubject<String?>).onNext(newValue ?: "")
    })

    val inputObservable: Observable<String?> = BehaviorSubject.create()

}