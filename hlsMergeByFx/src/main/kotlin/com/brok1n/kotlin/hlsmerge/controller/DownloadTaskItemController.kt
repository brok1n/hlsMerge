package com.brok1n.kotlin.hlsmerge.controller

import com.brok1n.kotlin.hlsmerge.data.DOWNLOAD_STATUS
import com.brok1n.kotlin.hlsmerge.data.DownloadTask
import com.brok1n.kotlin.hlsmerge.utils.log
import com.brok1n.kotlin.hlsmerge.utils.toRemainTime
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.Pane
import java.io.IOException
import java.util.*

class DownloadTaskItemController {

    @FXML
    lateinit var downloadTaskItemPane: Pane

    @FXML
    lateinit var fileNameLabel: Label

    @FXML
    lateinit var fileSizeLabel: Label

    @FXML
    lateinit var progressBar: ProgressBar

    @FXML
    lateinit var downloadSpeedLabel: Label

    @FXML
    lateinit var remainTimeLabel: Label

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/downloadTaskListItem.fxml"))
        fxmlLoader.setController(this)
        try{
            fxmlLoader.load<Parent>()
        }catch ( e: IOException){
            throw RuntimeException(e)
        }
    }

    fun initData( data: DownloadTask) {

        fileNameLabel.text = data.fileName
        fileSizeLabel.text = "${data.fileSize / 1024 / 1024}M"
        progressBar.progress = data.progress
        remainTimeLabel.text = toRemainTime(data.remainTime)

        when(data.downloadStatus) {
            1,2,3,4,6 -> {
                if ( data.downloadSpeed / 1024 > 1024 ) {
//                    val mb = "${data.downloadSpeed / 1024.0 / 1024.0}"
//                    var index = mb.indexOf(".")
//                    index = if ( index <= 0 ) { mb.length } else { index + 3 }
//                    if ( index >= mb.length ) index = mb.length
//                    downloadSpeedLabel.text = mb.substring(0, index) + "Mb/s"
                    val mb = (data.downloadSpeed / 1024.0 / 1024.0 * 1000).toInt() / 1000.0
                    downloadSpeedLabel.text = "$mb Mb/s"
                } else if (data.downloadSpeed > 1024 ) {
//                    val kb = "${data.downloadSpeed / 1024.0}"
//                    var index = kb.indexOf(".")
//                    index = if ( index <= 0 ) { kb.length } else { index + 3 }
//                    if ( index >= kb.length ) index = kb.length
//                    downloadSpeedLabel.text = kb.substring(0, index) + "Kb/s"

                    val kb = (data.downloadSpeed / 1024.0 * 1000).toInt()  / 1000.0
                    downloadSpeedLabel.text = "$kb Kb/s"
                } else {
                    downloadSpeedLabel.text = "${data.downloadSpeed} b/s"
                }
            }
            else -> {
                downloadSpeedLabel.text = DOWNLOAD_STATUS.getDownloadStatusName(data.downloadStatus)
            }
        }

    }

    fun getItem(): Pane {
        return downloadTaskItemPane
    }

}