package org.taskforce.episample.core

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

class LiveDataTriple<A, B, C>(liveA: LiveData<A>,
                              liveB: LiveData<B>,
                              liveC: LiveData<C>): MediatorLiveData<Triple<A, B, C>>() {
    private var hasA: Boolean = false
    private var hasB: Boolean = false
    private var hasC: Boolean = false

    private val merge = {
        if (hasA && hasB && hasC) {
            liveA.value?.let { unwrappedA ->
                liveB.value?.let { unwrappedB ->
                    liveC.value?.let { unwrappedC ->
                        postValue(Triple(unwrappedA, unwrappedB, unwrappedC))
                    }
                }
            }
        }
    }

    init {
        addSource(liveA) {
            hasA = true
            merge()
        }
        addSource(liveB) {
            hasB = true
            merge()
        }
        addSource(liveC) {
            hasC = true
            merge()
        }
    }
}