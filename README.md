[![](https://jitpack.io/v/seoft/AndroidRemoteConfig.svg)](https://jitpack.io/#seoft/AndroidRemoteConfig)


## AndroidRemoteConfig ?

It is recommended to use when you need to prepare simply without setting up the firebase console, or when you need to avoid the remote configuration limitation specification of Firebase.

However, firebase is more efficient if you need a complex configuration, use firebase's detailed features, or do not have a server that is guaranteed to run 24 hours a day, 365 days a year.



## Download

Add the JitPack repository in your build.gradle (top level module):

```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

And add next dependencies in the build.gradle of the module:

```gradle
android {
	/// ...

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.github.seoft:AndroidRemoteConfig:0.9.4'
}
```



## How to use



### 1. Prepare json to use as remote config in advance

#### example files

[Here](https://github.com/seoft/AndroidRemoteConfig/tree/dev/json_for_test)



#### json format

```
{
  "version": null,
  "message": "Set here normal message",
  "run": true,
  "etc": "Set here etc message if you need"
}
```



#### description

##### version

type : Int?

example : null or 1 or 20

detail : If the gradle version code is lower than the following value, the **onLowVersion** function is called. If null, **onLowVersion** function is not called.

code example :

```kotlin
    override fun onLowVersion(message: String?, etc: String?) {
        Log.d(TAG, "onLowVersion $message $etc")
        // message is "Get a new version from the Play Store"
        Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
        finish()
    }
```



##### message

type : String?

example : null or "Get a new version from the Play Store" or "The server is under maintenance until 11:00. Please excute the app after 11 o'clock"

detail : If it is not null, it is delivered as a message of onRun, onBlock, onLowVersion functions.



##### run

type : Boolean

example : true or false

detail : Value of whether the app is running.



##### etc

type : String?

example : null or "etc value"

detail : Define additional required values, it is delivered as a etc of onRun, onBlock, onLowVersion functions.





### 2. Create ProcessRemoteConfig using Builder

#### example

```kotlin
val processRemoteConfig1 =
    ProcessRemoteConfig.Builder("https://raw.githubusercontent.com/seoft/AndroidRemoteConfig/dev/json_for_test/normal_run_with_etc_message.json")
        .isDebug(true)
        .requestTimeoutSecond(30)
        .versionCode(BuildConfig.VERSION_CODE)
        .build()
```



#### description

##### Builder constructor(String)

address to request remote config value.



##### isDebug(Boolean)

whether the AndroidRemoteConfig module's logging.



##### isDebug(Boolean)

request timeout second



##### versionCode(Int)

The gradle version code of the current app to be compared with RemoteConfig



### 3. Set RemoteConfig Listener

#### OnRemoteConfigListener example

##### 1. normal case

```kotlin
//https://raw.githubusercontent.com/seoft/AndroidRemoteConfig/dev/json_for_test/normal_run_with_etc_message.json

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
```



##### 2. block launch app case

```kotlin
//https://raw.githubusercontent.com/seoft/AndroidRemoteConfig/dev/json_for_test/block_launch.json

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
```



##### 3. block and guide playstore only low version case

```kotlin
//https://raw.githubusercontent.com/seoft/AndroidRemoteConfig/dev/json_for_test/when_version_is_low.json

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
```



##### 4. using RxJava

```kotlin
//https://raw.githubusercontent.com/seoft/AndroidRemoteConfig/dev/json_for_test/when_version_is_low.json

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
})
```





### 4. After

Change the json value of the url that sends the request ProcessRemoteConfig when the server dies unexpectedly and you need to notify the notification, or when you need to forcibly update users of the previous version.
