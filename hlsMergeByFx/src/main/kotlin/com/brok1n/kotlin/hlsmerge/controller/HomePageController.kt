package com.brok1n.kotlin.hlsmerge.controller

import com.brok1n.kotlin.hlsmerge.WindowDragListener
import com.brok1n.kotlin.hlsmerge.log
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import kotlin.concurrent.thread


open class HomePageController {

    lateinit var scene: Scene
    lateinit var stage: Stage

    @FXML
    lateinit var titlePane: Pane

    @FXML
    fun initialize() {

        println(titlePane)




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