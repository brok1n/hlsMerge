package com.brok1n.kotlin.hlsmerge.utils

import com.brok1n.kotlin.hlsmerge.controller.DownloadTaskItemController
import com.brok1n.kotlin.hlsmerge.data.DownloadTask
import javafx.scene.control.ListCell

class DownloadTaskListCell: ListCell<DownloadTask>() {
    override fun updateItem(item: DownloadTask?, empty: Boolean) {
        super.updateItem(item, empty)
        if ( item == null ) {
            graphic = null
        } else {
            val downloadTaskItemController = DownloadTaskItemController()
            downloadTaskItemController.initData(item)
            graphic = downloadTaskItemController.getItem()
        }
    }
}