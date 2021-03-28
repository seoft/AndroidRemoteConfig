package kr.co.seoft.android_remote_config

sealed class RemoteConfigResult(val message: String?, val etc: String?) {
    class Run(message: String?, etc: String?) : RemoteConfigResult(message, etc)
    class Block(message: String?, etc: String?) : RemoteConfigResult(message, etc)
    class LowVersion(message: String?, etc: String?) : RemoteConfigResult(message, etc)
}