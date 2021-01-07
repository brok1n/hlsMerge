package com.brok1n.kotlin.hlsmerge.controller

import com.brok1n.kotlin.hlsmerge.DOWNLOAD_LABEL_TXT
import com.brok1n.kotlin.hlsmerge.HISTORY_LABEL_TXT
import com.brok1n.kotlin.hlsmerge.data.DOWNLOAD_STATUS
import com.brok1n.kotlin.hlsmerge.data.DataCenter
import com.brok1n.kotlin.hlsmerge.data.DownloadTask
import com.brok1n.kotlin.hlsmerge.net.NetManager
import com.brok1n.kotlin.hlsmerge.net.OnDownloadDetailListener
import com.brok1n.kotlin.hlsmerge.net.OnDownloadListener
import com.brok1n.kotlin.hlsmerge.net.OnRequestListener
import com.brok1n.kotlin.hlsmerge.utils.*
import javafx.animation.ScaleTransition
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Callback
import javafx.util.Duration
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.System.exit
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


open class HomePageController {

    lateinit var scene: Scene
    lateinit var stage: Stage

    //标题栏
    @FXML
    lateinit var titlePane: Pane

    //固定到最顶部复选框
    @FXML
    lateinit var alwaysTopCbox: CheckBox

    //下载按钮
    @FXML
    lateinit var downloadBtn: Pane

    //历史按钮
    @FXML
    lateinit var historyBtn: Pane

    //列表为空的提示图片
    @FXML
    lateinit var emptyTipImg: ImageView

    //新建按钮
    @FXML
    lateinit var createBtn: Pane

    //开始按钮
    @FXML
    lateinit var startBtn: ImageView

    //暂停按钮
    @FXML
    lateinit var pauseBtn: ImageView

    //删除按钮
    @FXML
    lateinit var deleteBtn: ImageView

    //提示Label
    @FXML
    lateinit var homeTipLabel: Label

    //下载列表ListView
    @FXML
    lateinit var downloadListView: ListView<DownloadTask>
    val downloadListObservableList = FXCollections.observableArrayList<DownloadTask>()

    //下载状态更新线程是否开启
    var updateDownloadStatusThreadRunning =  false

    //当前列表界面是否是下载界面
    var isDownloadList = true

    //下载速度
    @FXML
   lateinit var downloadSpeedLabel: Label

    //下载选项卡下载按钮文字
    @FXML
    lateinit var downloadTabLabel: Label

    //历史选项卡
    @FXML
    lateinit var historyTabLabel: Label

    fun init() {
        WindowDragListener(stage).enableDrag(titlePane)


        if ( DataCenter.instance.downloadList.isEmpty() ) {
            emptyTipImg.isVisible = true
        }

//        downloadListView.isMouseTransparent = true
        downloadListView.isFocusTraversable = false

        downloadListView.items = downloadListObservableList

        downloadListView.cellFactory = Callback<ListView<DownloadTask>, ListCell<DownloadTask>> {
            DownloadTaskListCell()
        }

        isDownloadList = true

        startDownloadManager()
        startTaskStatusUpdateThread()

    }

    /**
     * 最小化按钮被点击
     * */
    @FXML
    fun onMiniBtnClicked() {
        println("onMiniBtnClicked...............")
        stage.isIconified = true
    }

    /***
     * 关闭按钮被点击
     * */
    @FXML
    fun onCloseBtnClicked(){
        if (DataCenter.instance.downloadList.isEmpty() ) {
            DataCenter.instance.appIsRunning = false
            Platform.exit()
        } else {
            val alert = Alert(AlertType.CONFIRMATION)
            alert.title = "提示"
            alert.headerText = null
            alert.graphic = null
            alert.contentText = "还有未下载完毕的任务！确定要退出么？"

            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                // ... user chose OK
                alert.close()
                DataCenter.instance.appIsRunning = false
                stage.close()
                Platform.exit()
                exit(0)
            } else {
                // ... user chose CANCEL or closed the dialog
                alert.close()
            }
        }
    }

    /**
     * 固定到最顶部复选框被点击
     * */
    @FXML
    fun onAlwaysTopCboxClicked(){
        stage.isAlwaysOnTop = alwaysTopCbox.isSelected
    }

    /**
     * 下载按钮被点击
     * */
    @FXML
    fun onDownloadBtnClicked() {
        "下载按钮被点击 切换到下载界面".log()
        downloadBtn.style = "-fx-background-color: #DDEEFF"
        historyBtn.style = "-fx-background-color:  #C3E6FF"

        //切换界面
        if ( !isDownloadList ) {
            downloadListObservableList.clear()
            downloadListObservableList.addAll(DataCenter.instance.downloadList)
        }

        isDownloadList = true
    }

    /**
     * 历史按钮被点击
     * */
    @FXML
    fun onHistoryBtnClicked() {
        "历史按钮被点击  切换到历史界面".log()
        downloadBtn.style = "-fx-background-color: #C3E6FF"
        historyBtn.style = "-fx-background-color:  #DDEEFF"

        //切换页面
        if ( isDownloadList ) {
            downloadListObservableList.clear()
            downloadListObservableList.addAll(DataCenter.instance.historyList)
        }

        isDownloadList = false
    }

    /**
     * 新建按钮被点击
     * */
    @FXML
    fun onCreateBtnClicked(){
        "新建按钮被点击".log()
        DataCenter.instance.newTask = false

        ModelWindow.instance.showAddNewTaskWindow( stage )
        "新建按钮点击完成".log()

        if ( DataCenter.instance.newTask ) {

            if ( !DataCenter.instance.downloadList.isEmpty() ) {
                emptyTipImg.isVisible = false
            }

            downloadTabLabel.text = "$DOWNLOAD_LABEL_TXT(${downloadListObservableList.size})"
            historyTabLabel.text = "$HISTORY_LABEL_TXT(${DataCenter.instance.historyList.size})"

            //有新的下载任务
            homeTipLabel.text = "新增一个下载任务"
            homeTipLabel.style = "-fx-background-color: #46B9F0"
            homeTipLabel.textFill = Color.web("#FFFFFF")
            homeTipLabel.isVisible = true

            val scaleTransition = ScaleTransition()
            scaleTransition.duration = Duration.millis(200.0)
            scaleTransition.node = homeTipLabel
            scaleTransition.byX = 1.0
            scaleTransition.fromX = 0.0
            scaleTransition.play()

            //两秒之后 关闭提示
            postRunOnMainThread( 2000, object : Runnable {
                override fun run() {
                    homeTipLabel.isVisible = false
                }
            })

            val task = DataCenter.instance.downloadList.elementAt(DataCenter.instance.downloadList.size-1)
            Platform.runLater {
                downloadListObservableList.add(task)
            }

        }

        DataCenter.instance.newTask = false

    }

    private fun downloadTask(task: DownloadTask?) {

        if ( task == null ) return

        if ( DataCenter.instance.downloadThreadSize.get() >= DataCenter.instance.maxDownloadThreadSize ) {
            return
        }

        task.downloadStatus = DOWNLOAD_STATUS.getDownloadStatus(DOWNLOAD_STATUS.DOWNLOADING)
        DataCenter.instance.downloadThreadSize.incrementAndGet()

        val tmpUrl = task.url.toLowerCase()
        when {
            tmpUrl.contains(".m3u8") -> {
                downloadM3u8(task)
            }
            else -> {
                downloadFile(task)
            }
        }

    }

    private fun downloadFile(task: DownloadTask) {

        "开始下载文件".out()
        var outFileDir = DataCenter.instance.outDirPath + File.separator
        NetManager.instance.downloadAsync(task.url, outFileDir, object : OnDownloadDetailListener {
            override fun onUpdateFileSize(size: Long) {
                task.fileSize = size
            }

            override fun onUpdateSpeed(speed: Long) {
                task.downloadSpeed = speed
            }

            override fun ondownloadStart() {
                task.progress = 0.0
                task.downloadStatus = DOWNLOAD_STATUS.getDownloadStatus(DOWNLOAD_STATUS.DOWNLOADING)
            }

            override fun onDownloadSuccess() {
                task.progress = 1.0
                task.downloadStatus = DOWNLOAD_STATUS.getDownloadStatus(DOWNLOAD_STATUS.COMPLETED)
                downloadCompleted(task)
            }

            override fun onDownloading(progress: Int) {
                task.progress = progress.toDouble() / 100.0
            }

            override fun onDownloadFailed() {
                task.downloadStatus = DOWNLOAD_STATUS.getDownloadStatus(DOWNLOAD_STATUS.FAILED)
                DataCenter.instance.downloadThreadSize.decrementAndGet()
            }
        })
    }

    private fun downloadM3u8(task: DownloadTask) {
        "请求M3U8文件: ${task.url}".log()

        task.downloadStatus = DOWNLOAD_STATUS.DOWNLOADING_INT

        NetManager.instance.getAsync(task.url, object : OnRequestListener {
            override fun onSuccess(data: String) {
                "M3U8文件请求成功".log()
                data.log()

                "开始处理M3U8文件".log()
                //https://vid-egc.xnxx-cdn.com/videos/hls/7e/7a/9d/7e7a9dfcca751b6ac6f38c3d974c1ac5/hls-360p0.ts?Ct3StNtdyX3qhV4l7_0q0vjjswfT-JNXdfsYzHa5VACsOMIKsMtCZ7Ngr_Lzy6JQujRWjNMYrfS8ZwBwRBbwX5t8Ls4P9ilFt98eo5f7BXy8yApOJQCQCDQHPuqCrpd-C0_SxQ9VAAaWUufQiW5QhMBclg
                val lineList = data.split("\n")
                val fileName = FilenameUtils.getName(URL(task.url).path)
                val downloadBaseUrl = task.url.substring(0, task.url.indexOf(fileName))
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

                "M3U8文件处理完毕!  TS文件个数:${tsUrlList.size}".log()


                var outFileDir = DataCenter.instance.outDirPath + File.separator + md5(
                    task.url
                ) + File.separator

                File(outFileDir).mkdirs()
                File(outFileDir, fileName).createNewFile()
                File(outFileDir, fileName).writeText( task.url + "\n" + data)

                var downloadCount = 0
                var tsFileCount = tsUrlList.size
                val tmpList = ConcurrentLinkedQueue<String>()
                tmpList.addAll(tsUrlList)
                while ( DataCenter.instance.appIsRunning && !tmpList.isEmpty() ) {
                    val countDownLaunch = CountDownLatch(5)
                    for ( i in 1..5 ) {
                        thread {
                            try {
                                if ( !tmpList.isEmpty() ) {
                                    val tmpUrl = tmpList.poll()
                                    if ( tmpUrl.length > 10 ) {
                                        "开始下载:$tmpUrl".log()
                                        val status = NetManager.instance.download(tmpUrl, outFileDir, null)
                                        downloadCount++
                                        "下载完毕: $tmpUrl".log()
                                        task.progress = (downloadCount * 1.0  / tsFileCount)
                                        if ( !status ) {
                                            tsUrlList.remove(tmpUrl)
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

                "TS文件下载完毕! 共下载: $downloadCount 个TS文件".log()
                task.progress = 1.0
                task.downloadStatus = DOWNLOAD_STATUS.getDownloadStatus(DOWNLOAD_STATUS.COMPLETED)

                downloadCompleted(task)

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

            }

            override fun onFailed() {
                "请求失败".log()
                task.downloadStatus = DOWNLOAD_STATUS.getDownloadStatus(DOWNLOAD_STATUS.FAILED)
                DataCenter.instance.downloadThreadSize.decrementAndGet()
            }
        })

    }

    /**
     * 开启任务状态更新线程
     * */
    fun startTaskStatusUpdateThread() {
        //已经开启过了 就不用再开启了
        if ( updateDownloadStatusThreadRunning ) {
            return
        }
        updateDownloadStatusThreadRunning = true
        thread {
            try {
                while ( DataCenter.instance.appIsRunning ) {
//                    if ( DataCenter.instance.downloadThreadSize.get() > 0 ) {
                        updateData()
                        flushUi()
//                    }
                    //每隔0.5秒钟刷新一次数据
                    Thread.sleep(500)
                }

            }catch (e:Exception ){
                e.printStackTrace()
            }
        }
    }

    private fun flushUi() {
        Platform.runLater {
            if ( !downloadListObservableList.isEmpty() ) {
                downloadListView.refresh()
            }

            var speed = 0.0
            downloadListObservableList.forEach {
                if ( it.downloadStatus == DOWNLOAD_STATUS.DOWNLOADING_INT ) {
                    speed += it.downloadSpeed
                }
            }
            if ( speed < 1 ) {
                Platform.runLater {
                    downloadSpeedLabel.text = "0Kb/s"
                }
            } else if ( speed / 1024 > 1024 ) {
                val mb = "${speed / 1024.0 / 1024.0}"
                var index = mb.indexOf(".")
                index = if ( index <= 0 ) { mb.length } else { index + 3 }
                if ( index >= mb.length ) index = mb.length
                Platform.runLater {
                    downloadSpeedLabel.text = mb.substring(0, index) + "Mb/s"
                }
            } else {
                val kb = "${speed / 1024.0}"
                var index = kb.indexOf(".")
                index = if ( index <= 0 ) { kb.length } else { index + 3 }
                if ( index >= kb.length ) index = kb.length
                Platform.runLater {
                    downloadSpeedLabel.text = kb.substring(0, index) + "Kb/s"
                }
            }
        }
    }

    private fun updateData() {
        downloadListObservableList.forEach {
            if ( it.downloadSpeed > 0 ) {
                it.remainTime = ((it.fileSize * it.progress) / it.downloadSpeed * 1000).toLong()
            }
        }
    }


    private fun startDownloadManager() {

        try {
            thread {
                while ( DataCenter.instance.appIsRunning ) {

                    while ( DataCenter.instance.appIsRunning && DataCenter.instance.downloadList.isEmpty() ) {
                        Thread.sleep(1000)
                    }

                    if ( DataCenter.instance.downloadThreadSize.get() < DataCenter.instance.maxDownloadThreadSize ) {
                        for ( task in downloadListObservableList ) {
                            if ( task.downloadStatus == DOWNLOAD_STATUS.INIT_INT ) {
                                downloadTask(task)
                            }
                        }
                    }

                    Thread.sleep(1000)
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }

    fun downloadCompleted(task:DownloadTask){
        DataCenter.instance.downloadThreadSize.decrementAndGet()
        postRunOnMainThread(1500, Runnable {
            DataCenter.instance.historyList.add(task)
            DataCenter.instance.downloadList.remove(task)
            downloadListObservableList.remove(task)

            downloadTabLabel.text = "$DOWNLOAD_LABEL_TXT(${downloadListObservableList.size})"
            historyTabLabel.text = "$HISTORY_LABEL_TXT(${DataCenter.instance.historyList.size})"

            homeTipLabel.text = "1个任务下载完毕"
            homeTipLabel.style = "-fx-background-color: #46B9F0"
            homeTipLabel.textFill = Color.web("#FFFFFF")
            homeTipLabel.isVisible = true

            val scaleTransition = ScaleTransition()
            scaleTransition.duration = Duration.millis(200.0)
            scaleTransition.node = homeTipLabel
            scaleTransition.byX = 1.0
            scaleTransition.fromX = 0.0
            scaleTransition.play()

            //两秒之后 关闭提示
            postRunOnMainThread( 2000, object : Runnable {
                override fun run() {
                    homeTipLabel.isVisible = false
                }
            })

        })

    }

    /**
     * 开始按钮被点击
     * */
    @FXML
    fun onStartBtnClicked(){
        "开始按钮被点击".log()
    }

    /**
     * 暂停按钮被点击
     * */
    @FXML
    fun onPauseBtnClicked(){
        "暂停按钮被点击".log()

    }

    /**
     * 删除按钮被点击
     * */
    @FXML
    fun onDeleteBtnClicked(){
        "删除按钮被点击".log()
    }

    fun String.out(){
        this.log()
        if ( Thread.currentThread().name.contains("JavaFX Application") ) {
            //主线程
        } else {
            //非主线程
            Platform.runLater {

            }
        }

    }

}