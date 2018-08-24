package org.taskforce.episample.core

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

class LiveDataTriple<A, B, C>(liveA: LiveData<A>,
                              liveB: LiveData<B>,
                              liveC: LiveData<C>): MediatorLiveData<Triple<A?, B?, C?>>() {
    init {
        addSource(liveA) {
            value = Triple(it, liveB.value, liveC.value)
        }
        addSource(liveB) {
            value = Triple(liveA.value, it, liveC.value)
        }
        addSource(liveC) {
            value = Triple(liveA.value, liveB.value, it)
        }
    }
}