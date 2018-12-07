package com.brok1n.kotlin.hlsmerge.controller

import com.brok1n.kotlin.hlsmerge.ModelWindow
import com.brok1n.kotlin.hlsmerge.WindowDragListener
import com.brok1n.kotlin.hlsmerge.data.DataCenter
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.scene.layout.Pane
import javafx.stage.Stage

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

    fun init() {
        WindowDragListener(stage).enableDrag(titlePane)

        downloadPathEdt.text = DataCenter.instance.outDirPath

    }


    @FXML
    fun onCloseBtnClicked(){
        ModelWindow.instance.hideAddNewTaskWindow()
    }

}