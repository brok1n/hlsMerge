package com.brok1n.kotlin.hlsmerge.utils

import com.brok1n.kotlin.hlsmerge.ADD_TASK_WINDOW_TITLE
import com.brok1n.kotlin.hlsmerge.controller.AddNewTaskPageController
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle


open class ModelWindow {

    var addNewTaskWindow: Stage? = null

    fun showAddNewTaskWindow(owner: Stage) {

        val fxmlLoader = FXMLLoader(javaClass.getResource("../addNewTask.fxml"))
        val root = fxmlLoader.load<Parent>()

        val window = Stage()
        window.title = ADD_TASK_WINDOW_TITLE

//        primaryStage.scene = Scene(root, 800.0, 600.0)
        window.scene = Scene(root, 465.0, 230.0)
        window.initStyle(StageStyle.UNDECORATED) //设定窗口无边框

        window.initModality(Modality.WINDOW_MODAL)
        window.initOwner(owner)

        window.isResizable = false

        val addNewTaskPageController = fxmlLoader.getController<AddNewTaskPageController>()
        addNewTaskPageController.scene = window.scene
        addNewTaskPageController.stage = window

        addNewTaskPageController.init()

        window.x = owner.x - 60
        window.y = owner.y + 185

        addNewTaskWindow = window
        window.showAndWait()
    }

    fun hideAddNewTaskWindow() {
        addNewTaskWindow?.let {
            it.close()
        }
        addNewTaskWindow = null
    }


    companion object {
        val instance = ModelWindow()
    }
}
