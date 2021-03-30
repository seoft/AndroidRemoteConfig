package kr.co.seoft.android_remote_config

sealed class RemoteConfigResult {
    class Run(val message: String?, val etc: String?) : RemoteConfigResult()
    class Block(val message: String?, val etc: String?) : RemoteConfigResult()
    class LowVersion(val message: String?, val etc: String?) : RemoteConfigResult()
    class Fail(val throwable: Throwable) : RemoteConfigResult()
}