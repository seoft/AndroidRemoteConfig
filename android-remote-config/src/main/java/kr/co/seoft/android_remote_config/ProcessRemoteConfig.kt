package kr.co.seoft.android_remote_config

import android.util.Log
import com.google.gson.GsonBuilder
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ProcessRemoteConfig private constructor(private val builder: Builder) {

    companion object {
        private const val TAG = "ProcessRemoteConfigTAG"
    }

    private fun getOkHttp(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            if (builder.isDebug) addNetworkInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            )
            builder.requestTimeoutSecond.toLong().let {
                readTimeout(it, TimeUnit.SECONDS)
                writeTimeout(it, TimeUnit.SECONDS)
                connectTimeout(it, TimeUnit.SECONDS)
            }
        }.build()
    }

    fun requestRemoteConfig(onRemoteConfigListener: OnRemoteConfigListener) {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://this.is.not.used.dummy.text")
            .client(getOkHttp())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()

        retrofit.create(RemoteConfigApi::class.java).getRemoteConfig(builder.url)
            .enqueue(object : Callback<RemoteConfig> {
                override fun onFailure(call: Call<RemoteConfig>, t: Throwable) {
                    onRemoteConfigListener.onResponseFail(t)
                    if (builder.isDebug) Log.e(TAG, t.message ?: return)
                }

                override fun onResponse(
                    call: Call<RemoteConfig>,
                    response: Response<RemoteConfig>
                ) {
                    val nonNullRemoteConfig = response.body() ?: let {
                        if (builder.isDebug) {
                            Log.e(TAG, "response body is null")
                        }
                        return@onResponse
                    }

                    val immutableVersionCode = builder.versionCode

                    when {
                        (nonNullRemoteConfig.version != null && immutableVersionCode != null &&
                                nonNullRemoteConfig.version > immutableVersionCode) -> {
                            onRemoteConfigListener.onLowVersion(
                                nonNullRemoteConfig.message,
                                nonNullRemoteConfig.etc
                            )
                        }
                        nonNullRemoteConfig.run -> {
                            onRemoteConfigListener.onRun(
                                nonNullRemoteConfig.message,
                                nonNullRemoteConfig.etc
                            )
                        }
                        else -> {
                            onRemoteConfigListener.onBlock(
                                nonNullRemoteConfig.message,
                                nonNullRemoteConfig.etc
                            )
                        }
                    }
                }
            })

    }

    fun createRxSingleRemoteConfig(): Single<RemoteConfigResult> {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://this.is.not.used.dummy.text")
            .client(getOkHttp())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()

        return Single.create<RemoteConfigResult> { emitter ->
            retrofit.create(RemoteConfigApi::class.java).getRemoteConfig(builder.url)
                .enqueue(object : Callback<RemoteConfig> {
                    override fun onFailure(call: Call<RemoteConfig>, t: Throwable) {
                        emitter.onSuccess(RemoteConfigResult.Fail(t))
                        if (builder.isDebug) Log.e(TAG, t.message ?: return)
                    }

                    override fun onResponse(
                        call: Call<RemoteConfig>,
                        response: Response<RemoteConfig>
                    ) {
                        val nonNullRemoteConfig = response.body() ?: let {
                            if (builder.isDebug) Log.e(TAG, "response body is null")
                            emitter.onSuccess(RemoteConfigResult.Fail(Exception("response body is null")))
                            return@onResponse
                        }

                        val immutableVersionCode = builder.versionCode

                        when {
                            (nonNullRemoteConfig.version != null && immutableVersionCode != null &&
                                    nonNullRemoteConfig.version > immutableVersionCode) -> {
                                emitter.onSuccess(
                                    RemoteConfigResult.LowVersion(
                                        nonNullRemoteConfig.message,
                                        nonNullRemoteConfig.etc
                                    )
                                )
                            }
                            nonNullRemoteConfig.run -> {
                                emitter.onSuccess(
                                    RemoteConfigResult.Run(
                                        nonNullRemoteConfig.message,
                                        nonNullRemoteConfig.etc
                                    )
                                )
                            }
                            else -> {
                                emitter.onSuccess(
                                    RemoteConfigResult.Block(
                                        nonNullRemoteConfig.message,
                                        nonNullRemoteConfig.etc
                                    )
                                )
                            }
                        }
                    }
                })
        }
    }


    // Builder pattern ref : https://www.baeldung.com/kotlin-builder-pattern
    class Builder(val url: String) {

        var isDebug: Boolean = false
            private set

        var requestTimeoutSecond: Int = 10
            private set

        var versionCode: Int? = null
            private set

        fun isDebug(isDebug: Boolean) = apply { this.isDebug = isDebug }
        fun requestTimeoutSecond(requestTimeoutSecond: Int) =
            apply { this.requestTimeoutSecond = requestTimeoutSecond }

        fun versionCode(versionCode: Int) = apply { this.versionCode = versionCode }
        fun build() = ProcessRemoteConfig(this)
    }

}