package com.brok1n.kotlin.hlsmerge

import com.brok1n.kotlin.hlsmerge.controller.HomePageController
import com.brok1n.kotlin.hlsmerge.controller.MainPageController
import com.brok1n.kotlin.hlsmerge.data.DataCenter
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import javax.swing.filechooser.FileSystemView



class Main : Application() {

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {

        //获取桌面路径
        val fsv = FileSystemView.getFileSystemView() //注意了，这里重要的一句
        DataCenter.instance.outDirPath = fsv.homeDirectory.absolutePath
        println(DataCenter.instance.outDirPath)

        val fxmlLoader = FXMLLoader(javaClass.getResource("homePage.fxml"))
        val root = fxmlLoader.load<Parent>()
        primaryStage.title = APP_TITLE
//        primaryStage.scene = Scene(root, 800.0, 600.0)
        primaryStage.scene = Scene(root, 350.0, 600.0)
        primaryStage.initStyle(StageStyle.UNDECORATED);//设定窗口无边框

        primaryStage.icons.add(Image(javaClass.getResourceAsStream("/img/logo128.jpg")))


        val homePageController = fxmlLoader.getController<HomePageController>()
        homePageController.scene = primaryStage.scene
        homePageController.stage = primaryStage
        primaryStage.isResizable = false

//        val mainPageController = fxmlLoader.getController<MainPageController>()
//        mainPageController.scene = primaryStage.scene
//        mainPageController.stage = primaryStage
//        primaryStage.isResizable = false
//
//        primaryStage.setOnCloseRequest {
//            it.consume()
//            println("点击了关闭按钮")
//            DataCenter.instance.appIsRunning = false
//            Thread.sleep(300)
//            Platform.exit()
//        }

        primaryStage.show()

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}