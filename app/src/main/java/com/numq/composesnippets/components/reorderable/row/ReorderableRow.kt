package com.numq.composesnippets.components.reorderable.row

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

@Composable
fun <T : Any> ReorderableRow(
    modifier: Modifier,
    data: List<T>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    itemContent: @Composable (T) -> Unit
) {

    val listState = rememberLazyListState()

    var offsetX by remember { mutableStateOf(0f) }

    val dragDirection by derivedStateOf {
        when {
            offsetX < 0 -> DragDirection.LEFT
            offsetX > 0 -> DragDirection.RIGHT
            else -> DragDirection.NONE
        }
    }

    var draggableItem by remember { mutableStateOf<LazyListItemInfo?>(null) }

    val overlappedItem by derivedStateOf {
        draggableItem?.let { draggable ->
            val offset = draggable.offset
            val size = draggable.size
            listState.layoutInfo.visibleItemsInfo
                .filterNot { it.index == draggable.index }
                .firstOrNull { item ->
                    when (dragDirection) {
                        DragDirection.LEFT -> (offset + size + offsetX).toInt() in (item.offset..item.offset + item.size)
                        DragDirection.RIGHT -> (offset + offsetX).toInt() in (item.offset..item.offset + item.size)
                        else -> false
                    }
                }
        }
    }

    LazyRow(modifier, state = listState) {
        itemsIndexed(data) { index, item ->
            Box(
                modifier = Modifier
                    .zIndex(if (draggableItem?.index == index) 1f else 0f)
                    .graphicsLayer {
                        if (draggableItem?.index == index) {
                            alpha = .5f
                            translationX = offsetX
                            scaleY = .9f
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(onDragStart = {
                            listState.layoutInfo.visibleItemsInfo
                                .find { i ->
                                    i.index == index
                                }
                                ?.let {
                                    draggableItem = it
                                }
                        }, onDragCancel = {
                            offsetX = 0f
                            draggableItem = null
                        }, onDragEnd = {
                            draggableItem = null
                            offsetX = 0f
                        }) { change, (x, _) ->
                            change.consumeAllChanges()
                            offsetX += x
                            draggableItem?.let { draggable ->
                                overlappedItem?.let { overlapped ->
                                    onMove(draggable.index, overlapped.index)
                                    draggableItem = overlapped
                                    offsetX += ((draggable.index - overlapped.index) * draggable.size)
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                itemContent(item)
            }
        }
    }
}