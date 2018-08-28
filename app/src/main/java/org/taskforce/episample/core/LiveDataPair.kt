package org.taskforce.episample.core

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

class LiveDataPair<A, B>(liveA: LiveData<A>,
                         liveB: LiveData<B>) : MediatorLiveData<Pair<A, B>>() {

    var a: A? = null
    var b: B? = null

    private fun merge() {
        a?.let { unwrappedA ->
            b?.let { unwrappedB ->
                postValue(Pair(unwrappedA, unwrappedB))
            }
        }
    }

    init {
        addSource(liveA) {
            a = it
            merge()
        }
        addSource(liveB) {
            b = it
            merge()
        }
    }
}