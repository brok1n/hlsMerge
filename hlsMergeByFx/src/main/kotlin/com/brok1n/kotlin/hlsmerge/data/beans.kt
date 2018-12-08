package com.brok1n.kotlin.hlsmerge.data

data class TableItem( var code:String = "", var url:String = "" )



object DOWNLOAD_STATUS {
    val INIT = "未开始"
    val DOWNLOADING = "正在下载"
    val PAUSE = "已暂停"
    val STOP = "已停止"
    val DELETE = "已删除"
    val COMPLETED = "已完成"
    val FAILED = "下载失败"
    val INVALID = "未知错误"

    val INIT_INT = 0
    val DOWNLOADING_INT = 1
    val PAUSE_INT = 2
    val STOP_INT = 3
    val DELETE_INT = 4
    val COMPLETED_INT = 5
    val FAILED_INT = 6
    val INVALID_INT = 7

    fun getDownloadStatus(status:String):Int {
        when {
            status.equals(INIT) -> { return 0 }
            status.equals(DOWNLOADING) -> { return 1 }
            status.equals(PAUSE) -> { return 2 }
            status.equals(STOP) -> { return 3 }
            status.equals(DELETE) -> { return 4 }
            status.equals(COMPLETED) -> { return 5 }
            status.equals(FAILED) -> { return 6 }
        }
        return 7
    }

    fun getDownloadStatusName(status:Int):String {
        when(status) {
            0 -> { return INIT }
            1 -> { return DOWNLOADING }
            2 -> { return PAUSE }
            3 -> { return STOP }
            4 -> { return DELETE }
            5 -> { return COMPLETED }
            6 -> { return FAILED }
        }
        return INVALID
    }
}

data class Task(var url:String = "", var fileName: String = "", var fileSize:Long = 0L, var progress: Double = 0.0, var downloadSpeed: Long = 0, var remainTime:Long = 0, var downloadStatus:Int = 0)

data class DownloadTask( var url:String = "", var fileName: String = "", var fileSize:Long = 0L, var progress: Double = 0.0, var downloadSpeed: Long = 0, var remainTime:Long = 0, var downloadStatus:Int = 0, var downloadList: ArrayList<Task> = ArrayList() )




