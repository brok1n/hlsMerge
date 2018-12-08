package com.brok1n.kotlin.hlsmerge.utils

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.Stage

class WindowDragListener(var stage:Stage ): EventHandler<MouseEvent> {

    private var xOffset = 0.0
    private var yOffset = 0.0

    override fun handle(event: MouseEvent?) {
        event?.let {
            event.consume();
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                stage.setX(event.getScreenX() - xOffset);
                if(event.getScreenY() - yOffset < 0) {
                    stage.setY(0.0);
                }else {
                    stage.setY(event.getScreenY() - yOffset);
                }
            }
        }
    }

    fun enableDrag(node: Node) {
        node.setOnMousePressed(this)
        node.setOnMouseDragged(this)
    }
}