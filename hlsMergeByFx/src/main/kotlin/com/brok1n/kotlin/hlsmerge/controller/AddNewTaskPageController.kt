package com.brok1n.kotlin.hlsmerge.controller

import com.brok1n.kotlin.hlsmerge.utils.ModelWindow
import com.brok1n.kotlin.hlsmerge.SELECT_OUT_DIR_TITLE
import com.brok1n.kotlin.hlsmerge.utils.WindowDragListener
import com.brok1n.kotlin.hlsmerge.data.DataCenter
import com.brok1n.kotlin.hlsmerge.data.DownloadTask
import com.brok1n.kotlin.hlsmerge.utils.log
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.net.URL

open class AddNewTaskPageController {

    lateinit var scene: Scene
    lateinit var stage: Stage

    @FXML
    lateinit var titlePane: Pane

    //下载文件夹输入框
    @FXML
    lateinit var downloadPathEdt: TextField

    //下载网址输入框
    @FXML
    lateinit var downloadUrl:TextField

    //下载网址输入框提示文本框
    @FXML
    lateinit var downloadUrlTipLabel:Label

    //下载文件夹输入框提示文本框
    @FXML
    lateinit var downloadPathTipLabel:Label

    fun init() {
        WindowDragListener(stage).enableDrag(titlePane)

        downloadPathEdt.text = DataCenter.instance.outDirPath

        val outDirFile = File(DataCenter.instance.outDirPath)
        downloadPathTipLabel.text = "总大小:${outDirFile.totalSpace/1024/1024/1024}G 可用:${outDirFile.freeSpace/1024/1024/1024}G"

        DataCenter.instance.newTask = false

    }

    @FXML
    fun onCloseBtnClicked(){
        ModelWindow.instance.hideAddNewTaskWindow()
    }

    /**
     * 选择输出目录按钮被点击
     * */
    @FXML
    fun onSelectOutDirBtnClicked(){
        val dirChooser = DirectoryChooser()
        dirChooser.initialDirectory = File(DataCenter.instance.outDirPath)
        dirChooser.title = SELECT_OUT_DIR_TITLE
        val selectFile = dirChooser.showDialog(stage)

        "file:$selectFile".log()

        selectFile?.let {
            DataCenter.instance.outDirPath = it.absolutePath
            downloadPathEdt.text = it.absolutePath

            downloadPathTipLabel.text = "总大小:${it.totalSpace/1024/1024/1024}G 可用:${it.freeSpace/1024/1024/1024}G"

        }
    }

    /**
     * 下载按钮被点击
     * */
    @FXML
    fun onDownloadBtnClicked(){

        DataCenter.instance.newTask = false

        val url = downloadUrl.text.trim()
        if ( url.length <= 15 ) {
            //下载地址不合法
            downloadUrlTipLabel.textFill = Color.web("#F72E12")
            downloadUrlTipLabel.text = "下载地址不合法!"
            return
        } else {
            downloadUrlTipLabel.textFill = Color.web("#000000")
            downloadUrlTipLabel.text = ""
        }

        val task = DownloadTask(url)
        try {
            task.fileName = FilenameUtils.getName(URL(url).path)
        }catch (e:Exception) {
            //下载地址不合法
            downloadUrlTipLabel.textFill = Color.web("#F72E12")
            downloadUrlTipLabel.text = "下载地址不合法!"
            return
        }

        val fileName = FilenameUtils.getName(URL(task.url).path)
        val downloadBaseUrl = task.url.substring(0, task.url.indexOf(fileName) + fileName.length)

        var exists = false
        for (downloadTask in DataCenter.instance.downloadList) {
            if ( downloadTask.url.startsWith(downloadBaseUrl) ) {
                exists = true
            }
        }

        for (downloadTask in DataCenter.instance.historyList) {
            if ( downloadTask.url.startsWith(downloadBaseUrl) ) {
                exists = true
            }
        }

        if ( exists ) {
            //已存在相同任务
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "提示"
            alert.headerText = null
            alert.graphic = null
            alert.contentText = "该任务已存在, 是否重新下载?"

            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                // ... user chose OK
                alert.close()
                DataCenter.instance.downloadList.add(task)
                DataCenter.instance.newTask = true
                "添加一条下载任务:$url".log()
            } else {
                // ... user chose CANCEL or closed the dialog
                alert.close()
            }
        } else {
            DataCenter.instance.downloadList.add(task)
            DataCenter.instance.newTask = true
            "添加一条下载任务:$url".log()
        }

        ModelWindow.instance.hideAddNewTaskWindow()
    }



}