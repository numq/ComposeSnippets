package com.numq.composesnippets.components.reorderable.modifier

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

fun Modifier.reorderTarget(initialIndex: Int, reorderableState: ReorderableState) = composed {
    with(reorderableState) {
        LaunchedEffect(overscrollValue) {
            if (overscrollValue != 0f) listState.animateScrollBy(overscrollValue)
        }
        Modifier
            .run {
                if (draggableItem?.index == initialIndex) {
                    zIndex(1f)
                    graphicsLayer {
                        when (listType) {
                            ListType.Row -> translationX = offsetX
                            ListType.Column -> translationY = offsetY
                            ListType.Grid -> {
                                translationX = offsetX
                                translationY = offsetY
                            }
                        }
                        scaleX = .9f
                    }
                } else this
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
                    change.consumeAllChanges()
                    when (listType) {
                        ListType.Row -> offsetX += x
                        ListType.Column -> offsetY += y
                        ListType.Grid -> {
                            offsetX += x
                            offsetY += y
                        }
                    }
                }
            }
    }
}
