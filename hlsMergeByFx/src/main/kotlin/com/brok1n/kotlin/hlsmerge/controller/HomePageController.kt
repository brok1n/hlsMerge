package com.brok1n.kotlin.hlsmerge.controller

import com.brok1n.kotlin.hlsmerge.WindowDragListener
import com.brok1n.kotlin.hlsmerge.data.DataCenter
import com.brok1n.kotlin.hlsmerge.log
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.stage.Stage


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

    fun init() {
        WindowDragListener(stage).enableDrag(titlePane)


        if ( DataCenter.instance.downloadList.isEmpty() ) {
            emptyTipImg.isVisible = true
        }

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
            Platform.exit()
        } else {
            val alert = Alert(AlertType.CONFIRMATION)
            alert.title = "提示"
            alert.contentText = "还有未下载完毕的任务！确定要退出么？"

            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                // ... user chose OK
                alert.close()
                Platform.exit()
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


    }

    /**
     * 历史按钮被点击
     * */
    @FXML
    fun onHistoryBtnClicked() {
        "历史按钮被点击  切换到历史界面".log()
        downloadBtn.style = "-fx-background-color: #C3E6FF"
        historyBtn.style = "-fx-background-color:  #DDEEFF"


    }

    /**
     * 新建按钮被点击
     * */
    @FXML
    fun onCreateBtnClicked(){
        "新建按钮被点击".log()


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