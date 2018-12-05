package com.brok1n.kotlin.hlsmerge

import com.google.gson.Gson
import java.awt.SystemColor.text
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*


val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")


fun String.log(){
    println("LOG ${System.currentTimeMillis().toDateTimeStr()}:$this")
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
