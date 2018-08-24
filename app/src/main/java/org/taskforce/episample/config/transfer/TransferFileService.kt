package org.taskforce.episample.config.transfer

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TransferFileService {

    fun downloadFile(url: String) : Observable<ResponseBody> {

        val finalUrl = if (!url.startsWith("http://")) {
            "http://$url"
        } else {
            url
        }

        return Retrofit.Builder()
                .baseUrl(finalUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(TransferApi::class.java)
                .downloadFile(finalUrl)
                .observeOn(Schedulers.io())
    }

    companion object {
        fun constructFileLocation(bucket: TransferFileBucket, fileName: String) =
                if (bucket == TransferFileBucket.CONFIG) {
                    CONFIG_FOLDER + fileName
                } else {
                    CONFIG_BUILD_FOLDER + bucket.name.toLowerCase() + fileName
                }

        private const val CONFIG_BUILD_FOLDER = "/config/build/"
        private const val CONFIG_FOLDER = "/config/"
    }
}

enum class TransferFileBucket {
    CONFIG,
    LANGUAGE,
    LANDMARK,
    ENUMERATION,
    MAP_LAYER;

    companion object {
        val TRANSFER_KEY = TransferFileBucket::class.java.canonicalName
    }
}