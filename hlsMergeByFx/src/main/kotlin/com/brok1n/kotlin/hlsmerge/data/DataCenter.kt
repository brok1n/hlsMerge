package com.brok1n.kotlin.hlsmerge.data

import sun.rmi.runtime.Log
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.regex.Pattern


class DataCenter {

    //当前程序是否正在运行中
    var appIsRunning = false

    //m3u8地址
    var url = ""

    //输出默认路径  桌面
    var outDirPath = ""

    //下载列表
    var downloadList = ConcurrentLinkedQueue<String>()

    //是否添加了新的下载任务
    var newTask = false

    //历史列表
    var historyList = ConcurrentLinkedQueue<String>()

    fun getUrl(url:String):String {
        return "http://${url}/bookstore/api${url}"
    }

    companion object {
        @JvmStatic
        val instance = DataCenter()
    }
}