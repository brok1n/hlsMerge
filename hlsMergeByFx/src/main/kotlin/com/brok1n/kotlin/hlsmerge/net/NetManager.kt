package com.brok1n.kotlin.hlsmerge.net

import com.brok1n.kotlin.hlsmerge.log
import com.brok1n.kotlin.hlsmerge.okHttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

class NetManager private constructor(): INet {

    /**
     * 根据地址 获取网页内容
     * */
    override fun get(url: String): String? {
        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val result = response.body()?.string()

            if ( result != null ) {
                return result
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 根据地址 获取内容
     * */
    override fun getAsync(url: String, listener: OnRequestListener?) {
        try {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    listener?.onFailed()
                }

                override fun onResponse(call: Call, response: Response) {
                    listener?.onSuccess(response.body()!!.string())
                }
            })
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 下载文件
     * */
    override fun download(url: String, outDir: String, listener: OnDownloadListener?) {
        val fileName = FilenameUtils.getName(URL(url).path)
        val outFile = File(outDir, fileName)
        try {
            if ( !outFile.exists() ) {
                outFile.parentFile.mkdirs()
                outFile.createNewFile()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

        try {
            listener?.ondownloadStart()
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            var inp:InputStream = response.body()!!.byteStream()
            val dataSize = response.body()!!.contentLength()
            var buf = ByteArray(2048)
            var out: FileOutputStream? = null
            var sum = 0L
            var len = 0
            val off = 0
            try {
                out = FileOutputStream(outFile)
                while ( inp.read(buf).apply { len = this } > 0 ) {
                    out.write(buf, off, len)
                    sum += len.toLong()
                    val progress = (sum * 1.0f / dataSize * 100).toInt()
                    listener?.onDownloading(progress)
                }
                out.flush()
                out.close()
                listener?.onDownloadSuccess()
            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                try {
                    out?.close()
                }catch (e:Exception) {}
            }
        }catch (e:Exception ){
            e.printStackTrace()
        }
    }

    /**
     * 下载文件
     * */
    override fun downloadAsync(url: String, outDir: String, listener: OnDownloadListener?) {
        val fileName = FilenameUtils.getName(URL(url).path)
        val outFile = File(outDir, fileName)
        try {
            if ( !outFile.exists() ) {
                outFile.parentFile.mkdirs()
                outFile.createNewFile()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

        try {
            listener?.ondownloadStart()
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).enqueue(object :Callback{
                override fun onFailure(call: Call, e: IOException) {
                    //下载失败
                    listener?.onDownloadFailed()
                }

                override fun onResponse(call: Call, response: Response) {
                    var inp:InputStream = response.body()!!.byteStream()
                    val dataSize = response.body()!!.contentLength()
                    var buf = ByteArray(2048)
                    var out: FileOutputStream? = null
                    var sum = 0L
                    var len = 0
                    val off = 0
                    try {
                        out = FileOutputStream(outFile)
                        while ( inp.read(buf).apply { len = this } > 0 ) {
                            out.write(buf, off, len)
                            sum += len.toLong()
                            val progress = (sum * 1.0f / dataSize * 100).toInt()
                            listener?.onDownloading(progress)
                        }
                        out.flush()
                        out.close()
                        listener?.onDownloadSuccess()
                    }catch (e:Exception){
                        e.printStackTrace()
                    }finally {
                        try {
                            out?.close()
                        }catch (e:Exception) {}
                    }
                }
            })
        }catch (e:Exception ){
            e.printStackTrace()
        }
    }


    companion object {
        @JvmStatic
        val instance = NetManager()
    }
}