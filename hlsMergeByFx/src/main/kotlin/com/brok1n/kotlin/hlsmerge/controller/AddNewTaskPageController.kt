package com.brok1n.kotlin.hlsmerge.controller

import com.brok1n.kotlin.hlsmerge.ModelWindow
import com.brok1n.kotlin.hlsmerge.WindowDragListener
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

open class AddNewTaskPageController {

    lateinit var scene: Scene
    lateinit var stage: Stage

    @FXML
    lateinit var titlePane: Pane

    fun init() {
        WindowDragListener(stage).enableDrag(titlePane)
    }


    @FXML
    fun onCloseBtnClicked(){
        ModelWindow.instance.hideAddNewTaskWindow()
    }

}