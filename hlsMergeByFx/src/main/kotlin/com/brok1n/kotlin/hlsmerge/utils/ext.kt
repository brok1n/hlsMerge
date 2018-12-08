package com.brok1n.kotlin.hlsmerge.utils

import javafx.application.Platform
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")


fun String.log(){
    println("LOG ${System.currentTimeMillis().toDateTimeStr()}:$this")
}

fun postRun( delay: Long, run: Runnable ) {
    thread {
        try {
            Thread.sleep(delay)
        }catch (e:Exception){
            e.printStackTrace()
        }
        run.run()
    }
}

fun postRunOnMainThread( delay: Long, run: Runnable ) {
    thread {
        try {
            Thread.sleep(delay)
        }catch (e:Exception){
            e.printStackTrace()
        }
        Platform.runLater(run)
    }
}

fun Long.toDateTimeStr():String {
    return simpleDataFormat.format(Date(this))
}

fun md5_16bit(text:String):String {
    val str = md5(text)
    if ( str.length == 32 ) {
        return str.substring(8, 24)
    }
    return str
}

fun toRemainTime( time:Long ):String {
    if ( time <= 0 ) {
        return "00:00:00"
    }
    val h = time / 1000 / 60 / 60
    val m = time / 1000 / 60 % 60
    val s = time / 1000 % 60
    return "${fill(h, 2)}:${fill(m, 2)}:${fill(s, 2)}"
}
fun fill(d:Number, len:Int):String {
    val tmpCount = len - "$d".length
    if ( tmpCount <= 0 ) return "$d"
    var tmpStr = ""
    for ( i in 1..tmpCount){
        tmpStr += "0"
    }
    return tmpStr + d
}

fun md5(text:String ):String {
    try {
        //获取md5加密对象
        val instance = MessageDigest.getInstance("MD5")
        //对字符串加密，返回字节数组
        val digest:ByteArray = instance.digest(text.toByteArray())
        var sb : StringBuffer = StringBuffer()
        for (b in digest) {
            //获取低八位有效值
            var i :Int = b.toInt() and 0xff
            //将整数转化为16进制
            var hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                //如果是一位的话，补0
                hexString = "0" + hexString
            }
            sb.append(hexString)
        }
        return sb.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}


fun String.removeHtmlTag():String {
    try {



    }catch (e:Exception) {
        e.printStackTrace()
    }
    return this
}
