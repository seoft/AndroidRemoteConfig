package kr.co.seoft.android_remote_config

interface OnRemoteConfigListener {
    fun onRun(message: String?, etc: String?)
    fun onBlock(message: String?, etc: String?) = Unit
    fun onResponseFail(throwable: Throwable) = Unit
    fun onLowVersion(message: String?, etc: String?) = Unit
}