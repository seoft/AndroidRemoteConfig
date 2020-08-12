package kr.co.seoft.android_remote_config

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface RemoteConfigApi {

    @GET
    fun getRemoteConfig(@Url url: String): Call<RemoteConfig>
}