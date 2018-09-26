package org.taskforce.episample.core

import android.arch.lifecycle.MutableLiveData

class InitializedLiveData<T>(t: T): MutableLiveData<T>() {
    private var backingValue = t

    override fun setValue(value: T) {
        super.setValue(value)
        backingValue = value
    }

    override fun getValue(): T {
        return super.getValue() ?: backingValue
    }
}
