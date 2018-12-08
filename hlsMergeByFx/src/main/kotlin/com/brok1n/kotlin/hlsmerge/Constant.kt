package com.brok1n.kotlin.hlsmerge

import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.MediaType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val APP_TITLE = "HLS(M3U8)视频下载合并工具 V0.0.1 by brok1n"
val ADD_TASK_WINDOW_TITLE = "新建下载"
val SELECT_OUT_DIR_TITLE = "选择文件夹"


val jsonType = MediaType.parse("application/json; charset=utf-8")
val okHttpClient = OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).build()
val gson = Gson()
val jsonParser = JsonParser()


