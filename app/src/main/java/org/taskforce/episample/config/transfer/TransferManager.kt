package org.taskforce.episample.config.transfer

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.text.format.Formatter
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


class TransferManager(context: Context) {

    private val webServer = TransferWebServer()

    val transferFileService = TransferFileService()

    private val transferStateSubject = BehaviorSubject.create<TransferState>()

    @Suppress("DEPRECATION")
    private var ipAddress =
            Formatter.formatIpAddress(
                    (context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager)
                            .connectionInfo.ipAddress) + ":${webServer.port}"
        set(value) {
            field = value
            publishTransferState()
        }

    private var connected = false
        set(value) {
            field = value
            publishTransferState()
        }

    init {
        val networkInfo = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                .activeNetworkInfo
        connected = networkInfo?.isConnectedOrConnecting == true && networkInfo.type == ConnectivityManager.TYPE_WIFI
    }

    private var inProgress = false
        set(value) {
            field = value
            publishTransferState()
        }

    val transferStateObservable
        get() = transferStateSubject as Observable<TransferState>

    init {
        publishTransferState()
    }

    private fun publishTransferState() {
        transferStateSubject.onNext(TransferState(connected, inProgress, ipAddress))
    }
}

data class TransferState(val connected: Boolean,
                         val inProgress: Boolean,
                         val ipAddress: String)