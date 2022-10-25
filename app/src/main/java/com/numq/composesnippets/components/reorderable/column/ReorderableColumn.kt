package com.numq.composesnippets.components.reorderable.column

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


@Composable
fun <T : Any> ReorderableColumn(
    modifier: Modifier,
    data: List<T>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    itemContent: @Composable (T) -> Unit
) {

    val listState = rememberLazyListState()

    var offsetY by remember { mutableStateOf(0f) }

    val dragDirection by derivedStateOf {
        when {
            offsetY < 0 -> DragDirection.UP
            offsetY > 0 -> DragDirection.DOWN
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
                        DragDirection.UP -> (offset + size + offsetY).toInt() in (item.offset..item.offset + item.size)
                        DragDirection.DOWN -> (offset + offsetY).toInt() in (item.offset..item.offset + item.size)
                        else -> false
                    }
                }
        }
    }

    LazyColumn(modifier, state = listState) {
        itemsIndexed(data) { index, item ->
            Row(
                Modifier
                    .zIndex(if (draggableItem?.index == index) 1f else 0f)
                    .graphicsLayer {
                        if (draggableItem?.index == index) {
                            translationY = offsetY
                            scaleX = .9f
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .graphicsLayer {
                            if (draggableItem?.index == index) {
                                alpha = .5f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    itemContent(item)
                }
                Box(
                    Modifier
                        .fillMaxHeight()
                        .padding(8.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(onDragStart = {
                                listState.layoutInfo.visibleItemsInfo
                                    .find { i ->
                                        i.index == index
                                    }
                                    ?.let {
                                        draggableItem = it
                                    }
                            }, onDragCancel = {
                                offsetY = 0f
                                draggableItem = null
                            }, onDragEnd = {
                                draggableItem = null
                                offsetY = 0f
                            }) { change, (_, y) ->
                                change.consumeAllChanges()
                                offsetY += y
                                draggableItem?.let { draggable ->
                                    overlappedItem?.let { overlapped ->
                                        onMove(draggable.index, overlapped.index)
                                        draggableItem = overlapped
                                        offsetY += ((draggable.index - overlapped.index) * draggable.size)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Reorder,
                        "reorder column"
                    )
                }
            }
        }
    }
}