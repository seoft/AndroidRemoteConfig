package kr.co.seoft.android_remote_config_sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kr.co.seoft.android_remote_config.OnRemoteConfigListener
import kr.co.seoft.android_remote_config.ProcessRemoteConfig
import kr.co.seoft.android_remote_config.RemoteConfigResult

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivityTAG"
    }

    enum class TestType {
        NORMAL_RUN_WITH_ETC_MESSAGE,
        BLOCK_LAUNCH_APP,
        WHEN_VERSION_IS_LOW,
        RX_SINGLE
    }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /**
         * set here if run each case
         */
        val testType = TestType.RX_SINGLE
        processTest(testType)

    }

    private fun processTest(testType: TestType) {
        when (testType) {
            TestType.NORMAL_RUN_WITH_ETC_MESSAGE -> {
                val processRemoteConfig1 =
                    ProcessRemoteConfig.Builder("https://raw.githubusercontent.com/seoft/AndroidRemoteConfig/dev/json_for_test/normal_run_with_etc_message.json")
                        .isDebug(true)
                        .requestTimeoutSecond(30)
                        .versionCode(BuildConfig.VERSION_CODE)
                        .build()

                processRemoteConfig1.requestRemoteConfig(object : OnRemoteConfigListener {

                    override fun onRun(message: String?, etc: String?) {
                        Log.d(TAG, "onRun $message $etc")
                        Toast.makeText(baseContext, "onRun $message $etc", Toast.LENGTH_LONG).show()
                        startActivity(Intent(baseContext, MainActivity::class.java))
                        finish()
                    }

                    override fun onResponseFail(throwable: Throwable) {
                        // try when failed
                    }
                })
            }
            TestType.BLOCK_LAUNCH_APP -> {
                val processRemoteConfig2 =
                    ProcessRemoteConfig.Builder("https://raw.githubusercontent.com/seoft/AndroidRemoteConfig/dev/json_for_test/block_launch.json")
                        .isDebug(true)
                        .requestTimeoutSecond(30)
                        .versionCode(BuildConfig.VERSION_CODE)
                        .build()

                processRemoteConfig2.requestRemoteConfig(object : OnRemoteConfigListener {

                    override fun onRun(message: String?, etc: String?) {
                        Log.d(TAG, "onRun $message $etc")
                        startActivity(Intent(baseContext, MainActivity::class.java))
                        finish()
                    }

                    override fun onBlock(message: String?, etc: String?) {
                        Log.d(TAG, "onBlock $message $etc")
                        Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
                        finish()
                    }

                })
            }
            TestType.WHEN_VERSION_IS_LOW -> {
                val processRemoteConfig3 =
                    ProcessRemoteConfig.Builder("https://raw.githubusercontent.com/seoft/AndroidRemoteConfig/dev/json_for_test/when_version_is_low.json")
                        .isDebug(true)
                        .requestTimeoutSecond(30)
                        .versionCode(BuildConfig.VERSION_CODE)
                        .build()

                processRemoteConfig3.requestRemoteConfig(object : OnRemoteConfigListener {
                    override fun onRun(message: String?, etc: String?) {
                        Log.d(TAG, "onRun $message $etc")
                        startActivity(Intent(baseContext, MainActivity::class.java))
                        finish()
                    }

                    override fun onBlock(message: String?, etc: String?) {
                        Log.d(TAG, "onBlock $message $etc")
                        Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
                        finish()
                    }

                    override fun onLowVersion(message: String?, etc: String?) {
                        Log.d(TAG, "onLowVersion $message $etc")
                        // message is "Get a new version from the Play Store"
                        Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
                        finish()
                    }
                })
            }
            TestType.RX_SINGLE -> {

                // Same url to processRemoteConfig1
                val processRemoteConfig4 =
                    ProcessRemoteConfig.Builder("https://raw.githubusercontent.com/seoft/AndroidRemoteConfig/dev/json_for_test/normal_run_with_etc_message.json")
                        .isDebug(true)
                        .requestTimeoutSecond(30)
                        .versionCode(BuildConfig.VERSION_CODE)
                        .build()

                processRemoteConfig4.createRxSingleRemoteConfig()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            when (this) {
                                is RemoteConfigResult.Run -> {
                                    Log.d(TAG, "onRun $message $etc")
                                    startActivity(Intent(baseContext, MainActivity::class.java))
                                    finish()
                                }
                                is RemoteConfigResult.LowVersion -> {
                                    Log.d(TAG, "onLowVersion $message $etc")
                                    // message is "Get a new version from the Play Store"
                                    Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
                                    finish()
                                }
                                is RemoteConfigResult.Block -> {
                                    Log.d(TAG, "onBlock $message $etc")
                                    Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
                                    finish()
                                }
                                is RemoteConfigResult.Fail -> {
                                    // try when failed
                                    this.throwable.printStackTrace()
                                }
                            }
                        }
                    }, {
                        it.printStackTrace()
                    })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}