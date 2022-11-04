package com.numq.composesnippets.components.reorderable.modifier

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

fun Modifier.reorderTarget(initialIndex: Int, reorderableState: ReorderableState) = then(
    with(reorderableState) {
        Modifier
            .run {
                if (draggableItem?.index == initialIndex) {
                    zIndex(1f)
                    graphicsLayer {
                        when (listState.layoutInfo.orientation) {
                            Orientation.Horizontal -> translationX = offsetX
                            Orientation.Vertical -> translationY = offsetY
                        }
                        scaleX = .9f
                    }
                } else zIndex(-1f)
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(onDragStart = {
                    listState.layoutInfo.visibleItemsInfo
                        .find { i ->
                            i.index == initialIndex
                        }
                        ?.let {
                            draggableItem = it
                        }
                }, onDragCancel = {
                    offsetX = 0f
                    offsetY = 0f
                    draggableItem = null
                }, onDragEnd = {
                    dropIndex?.let { onMove(initialIndex, it) }
                    draggableItem = null
                    offsetX = 0f
                    offsetY = 0f
                }) { change, (x, y) ->
                    change.consume()
                    when (listState.layoutInfo.orientation) {
                        Orientation.Horizontal -> offsetX += x
                        Orientation.Vertical -> offsetY += y
                    }
                }
            }
    })
