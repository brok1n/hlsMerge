package com.brok1n.kotlin.hlsmerge.controller

import com.brok1n.kotlin.hlsmerge.data.DataCenter
import com.brok1n.kotlin.hlsmerge.data.TableItem
import com.brok1n.kotlin.hlsmerge.log
import com.brok1n.kotlin.hlsmerge.md5
import com.brok1n.kotlin.hlsmerge.net.NetManager
import com.brok1n.kotlin.hlsmerge.net.OnDownloadListener
import com.brok1n.kotlin.hlsmerge.net.OnRequestListener
import com.brok1n.kotlin.hlsmerge.toDateTimeStr
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.stage.Stage
import javafx.util.Callback
import org.apache.commons.io.FilenameUtils
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import java.io.InputStreamReader
import java.io.BufferedReader
import java.util.*


open class MainPageController {

    lateinit var scene: Scene
    lateinit var stage: Stage

    //MU38地址输入框
    @FXML
    lateinit var urlEdt:TextField

    //开始按钮
    @FXML
    lateinit var startBtn:Button

    //停止按钮
    @FXML
    lateinit var stopBtn:Button

    //进度条
    @FXML
    lateinit var progressBar: ProgressBar

    //tableView
    @FXML
    lateinit var tableView: TableView<TableItem>

    //textarea
    @FXML
    lateinit var textArea: TextArea

    //输出目录
    @FXML
    lateinit var outDirLabel:Label

    //下载完成文件个数
    var downloadSuccessFileCount = 0

    //code
    @FXML
    lateinit var codeCol: TableColumn<TableItem, String>
    //url
    @FXML
    lateinit var urlCol: TableColumn<TableItem, String>


   val tableObservableList = FXCollections.observableArrayList<TableItem>()

    @FXML
    fun initialize() {
        outDirLabel.text = DataCenter.instance.outDirPath

        codeCol.cellValueFactory =
                Callback<TableColumn.CellDataFeatures<TableItem, String>, ObservableValue<String>> { param ->
                    if ( param != null && param.value != null ) {
                        SimpleStringProperty( param.value.code)
                    } else {
                        SimpleStringProperty("")
                    }
                }

        urlCol.cellValueFactory =
                Callback<TableColumn.CellDataFeatures<TableItem, String>, ObservableValue<String>> { param ->
                    if ( param != null && param.value != null ) {
                        SimpleStringProperty( param.value.url)
                    } else {
                        SimpleStringProperty("")
                    }
                }


        tableView.items = tableObservableList

    }


    @FXML
    fun onStartBtnClicked(){

        val url = urlEdt.text.trim()
        if ( url == null || url.length <= 10 ) {
            "请输入正确的地址!!!".out()
            return
        }

        startBtn.isDisable = true
        stopBtn.isDisable = false

        DataCenter.instance.url = url

        "开始".out()

        DataCenter.instance.appIsRunning = true

        val tmpUrl = url.toLowerCase()
        when {
            tmpUrl.contains(".m3u8") -> {
                downloadM3u8()
            }
            tmpUrl.contains( ".ts") or
                    tmpUrl.contains(".mp3") or
                    tmpUrl.contains(".mp4") or
                    tmpUrl.contains(".avi") or
                    tmpUrl.contains(".rmvb") or
                    tmpUrl.contains(".mkv") or
                    tmpUrl.contains(".flv") or
                    tmpUrl.contains(".vob") or
                    tmpUrl.contains(".mpg") or
                    tmpUrl.contains(".mpeg") or
                    tmpUrl.contains(".3gp") or
                    tmpUrl.contains(".mov") or
                    tmpUrl.contains(".rm") or
                    tmpUrl.contains(".swf")
            -> {
                downloadVideo()
            }
        }

    }

    fun downloadM3u8(){
        "请求M3U8文件: ${DataCenter.instance.url}".out()

        downloadSuccessFileCount = 0

        NetManager.instance.getAsync(DataCenter.instance.url, object :OnRequestListener {
            override fun onSuccess(data: String) {
                "M3U8文件请求成功".out()
                data.log()

                "开始处理M3U8文件".out()
                //https://vid-egc.xnxx-cdn.com/videos/hls/7e/7a/9d/7e7a9dfcca751b6ac6f38c3d974c1ac5/hls-360p0.ts?Ct3StNtdyX3qhV4l7_0q0vjjswfT-JNXdfsYzHa5VACsOMIKsMtCZ7Ngr_Lzy6JQujRWjNMYrfS8ZwBwRBbwX5t8Ls4P9ilFt98eo5f7BXy8yApOJQCQCDQHPuqCrpd-C0_SxQ9VAAaWUufQiW5QhMBclg
                val lineList = data.split("\n")
                val fileName = FilenameUtils.getName(URL(DataCenter.instance.url).path)
                val downloadBaseUrl = DataCenter.instance.url.substring(0, DataCenter.instance.url.indexOf(fileName))
                var i = 0

                var tsUrlList = ConcurrentLinkedQueue<String>()

                while (i < lineList.size) {
                    var str = lineList[i]
                    if (str.startsWith("#EXTINF:")) {
                        i++
                        str = lineList[i]
                        if (str.trim().startsWith("http://") || str.trim().startsWith("https://")) {
                            tsUrlList.add(str)
                        } else {
                            tsUrlList.add(downloadBaseUrl + str)
                        }
                    }
                    i++
                }

                "M3U8文件处理完毕!  TS文件个数:${tsUrlList.size}".out()


                var outFileDir = DataCenter.instance.outDirPath + File.separator + md5(DataCenter.instance.url) + File.separator

                File(outFileDir).mkdirs()
                File(outFileDir, fileName).createNewFile()
                File(outFileDir, fileName).writeText(DataCenter.instance.url + "\n" + data)

                var downloadCount = 0
                var tsFileCount = tsUrlList.size
                val tmpList = ConcurrentLinkedQueue<String>()
                tmpList.addAll(tsUrlList)
                while ( !tmpList.isEmpty() ) {
                    val countDownLaunch = CountDownLatch(5)
                    for ( i in 1..5 ) {
                        thread {
                            try {
                                if ( !tmpList.isEmpty() ) {
                                    val tmpUrl = tmpList.poll()
                                    if ( tmpUrl.length > 10 ) {
                                        downloadFile(tmpUrl, outFileDir)
                                        downloadCount++
                                        Platform.runLater {
                                            progressBar.progress = (downloadCount * 1.0  / tsFileCount)
                                            "下载完毕: $tmpUrl".out()
                                        }
                                    }
                                }
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
                            countDownLaunch.countDown()
                        }
                    }
                    countDownLaunch.await()
                }

                "TS文件下载完毕! 共下载: $downloadCount 个TS文件".out()

                //视频合并

                var ffCmd = "ffmpeg -i \"concat:"
                val tsList = tsUrlList

                if ( tsList.size > 499 ) {
                    for ( i in 0..499 ) {
                        ffCmd += FilenameUtils.getName(URL(tsList.elementAt(i)).path)
                        if (i<tsList.size-1){
                            ffCmd += "|"
                        }
                    }
                    ffCmd += "\""
                    ffCmd+=" -c copy output_all111.ts"

                    ffCmd.out()

                    val batFile = File(outFileDir, "merge111.bat")
                    val out = FileOutputStream(batFile)
                    out.write(ffCmd.toByteArray())
                    out.flush()
                    out.close()

                    ffCmd = "ffmpeg -i \"concat:output_all111.ts|"

                    for ( i in 500 until tsList.size ) {
                        ffCmd += FilenameUtils.getName(URL(tsList.elementAt(i)).path)
                        if (i<tsList.size-1){
                            ffCmd += "|"
                        }
                    }
                    ffCmd += "\""
                    ffCmd+=" -c copy output_all.ts"

                    ffCmd.out()

                    val batFile1 = File(outFileDir, "merge222.bat")
                    val out1 = FileOutputStream(batFile1)
                    out1.write(ffCmd.toByteArray())
                    out1.flush()
                    out1.close()

                } else {
                    for ( i in 0 until tsList.size ) {
                        ffCmd += FilenameUtils.getName(URL(tsList.elementAt(i)).path)
                        if (i<tsList.size-1){
                            ffCmd += "|"
                        }
                    }
                    ffCmd += "\""
                    ffCmd+=" -c copy output_all.ts"

                    ffCmd.out()

                    val batFile = File(outFileDir, "merge.bat")
                    val out = FileOutputStream(batFile)
                    out.write(ffCmd.toByteArray())
                    out.flush()
                    out.close()
                }

                processCompleted()


            }

            override fun onFailed() {
                "请求失败".out()
            }
        })
    }

    fun downloadFile(url:String , outFileDir:String ) {
        NetManager.instance.download(url, outFileDir, object : OnDownloadListener{
            override fun ondownloadStart() {
            }

            override fun onDownloadSuccess() {
                Platform.runLater {
                    tableObservableList.add(TableItem("200", url))
                    tableView.scrollTo(tableObservableList.lastIndex)
                }
            }

            override fun onDownloading(progress: Int) {

            }

            override fun onDownloadFailed() {
                Platform.runLater {
                    tableObservableList.add(TableItem("400", url))
                    tableView.scrollTo(tableObservableList.lastIndex)
                }
            }
        })
    }


    fun downloadVideo(){
        "开始下载视频文件".out()
        var outFileDir = DataCenter.instance.outDirPath + File.separator + md5(DataCenter.instance.url + System.currentTimeMillis()) + File.separator
        NetManager.instance.downloadAsync(DataCenter.instance.url, outFileDir, object : OnDownloadListener{
            override fun ondownloadStart() {
                Platform.runLater {
                    progressBar.progress = 0.0
                    "开始下载".out()
                }

            }

            override fun onDownloadSuccess() {
                Platform.runLater {
                    progressBar.progress = 1.0
                    "下载完毕".out()
                }
            }

            override fun onDownloading(progress: Int) {
                Platform.runLater {
                    progressBar.progress = progress.toDouble() / 100.0
                }
            }

            override fun onDownloadFailed() {
                Platform.runLater {
                    "下载失败!".out()
                }
            }
        })
    }


    fun processCompleted(){

        stopBtn.isDisable = true
        startBtn.isDisable = false

        stage.requestFocus()

    }

    @FXML
    fun onStopBtnClicked(){
        DataCenter.instance.appIsRunning = false
        stopBtn.isDisable = true
        startBtn.isDisable = false
    }


    @FXML
    fun updateOutDirBtnClicked(){

    }

    @FXML
    fun clearBtnClicked(){
        urlEdt.text = ""
        tableObservableList.clear()
        textArea.text = ""
        progressBar.progress = 0.0
    }

    fun String.out(){
        this.log()
        if ( Thread.currentThread().name.contains("JavaFX Application") ) {
            textArea.appendText("${System.currentTimeMillis().toDateTimeStr()}:$this\n")
        } else {
            Platform.runLater {
                textArea.appendText("${System.currentTimeMillis().toDateTimeStr()}:$this\n")
            }
        }

    }

}