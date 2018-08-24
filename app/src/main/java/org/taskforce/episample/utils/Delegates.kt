package org.taskforce.episample.utils

import android.databinding.BaseObservable
import org.taskforce.episample.BR
import kotlin.reflect.KProperty

/**
 * Only use this delegated bindable with variables that have been annotated as @get:Bindable.
 * Failure to do so will result in a runtime exception as this delegate tries to update a
 * bindable resource that is non-existent.
 */
class DelegatedBindable<T>(private var value: T,
                           private val observer: BaseObservable,
                           private val expression: ((oldValue: T, newValue: T) -> Unit)? = null) {

    /**
     * Do not access this unless you know exactly what you're doing.
     */
    var bindingTarget: Int = -1

    operator fun getValue(thisRef: Any?, p: KProperty<*>) = value

    operator fun setValue(thisRef: Any?, p: KProperty<*>, v: T) {

        val oldValue = value
        value = v
        if (bindingTarget == -1) {
            bindingTarget = BR::class.java.fields.filter {
                it.name == p.name
            }[0].getInt(null)
        }
        observer.notifyPropertyChanged(bindingTarget)
        expression?.invoke(oldValue, value)
    }
}

fun <T> BaseObservable.bindDelegate(value: T,
                                    expression: ((oldValue: T, newValue: T) -> Unit)? = null):
        DelegatedBindable<T> =
        DelegatedBindable(value, this, expression)