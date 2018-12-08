package com.brok1n.kotlin.hlsmerge.net

import com.google.gson.JsonObject
import com.sun.org.apache.xpath.internal.operations.Bool


interface INet {

    /**
     * 根据地址 获取响应内容
     * */
    fun get(url: String): String?

    /**
     * 根据地址 获取内容
     * */
    fun getAsync(url:String, listener: OnRequestListener?)

    /**
     *  下载文件
     * */
    fun downloadAsync(url: String, outDir: String, listener: OnDownloadListener?)

    /**
     *  下载文件
     * */
    fun download(url: String, outDir: String, listener: OnDownloadListener?):Boolean

}


interface OnRequestListener {
    /**
     * 请求成功
     */
    fun onSuccess( data:String )

    /**
     * 请求失败
     */
    fun onFailed()
}


interface OnDownloadListener {

    /**
     * 开始下载
     * */
    fun ondownloadStart()

    /**
     * 下载成功
     */
    fun onDownloadSuccess()

    /**
     * @param progress
     * 下载进度
     */
    fun onDownloading(progress: Int)

    /**
     * 下载失败
     */
    fun onDownloadFailed()
}

interface OnDownloadDetailListener: OnDownloadListener {

    /**
     * 文件大小
     * */
    fun onUpdateFileSize(size:Long)

    /**
     * 下载速度
     * */
    fun onUpdateSpeed(speed:Long)
}