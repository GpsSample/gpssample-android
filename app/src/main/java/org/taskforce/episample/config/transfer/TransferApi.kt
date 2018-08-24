package org.taskforce.episample.config.transfer

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface TransferApi {

    @Streaming
    @GET
    fun downloadFile(@Url url: String): Observable<ResponseBody>

}