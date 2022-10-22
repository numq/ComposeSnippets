package com.numq.composesnippets.components.reorder

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
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
fun <T> ReorderableColumn(
    items: MutableList<T>,
    onMove: (Int, Int) -> Unit,
    onItemContent: @Composable (T) -> Unit
) {

    val lazyListState = rememberLazyListState()

    var draggingItem by remember { mutableStateOf<LazyListItemInfo?>(null) }

    var offsetY by remember { mutableStateOf(0f) }

    val hoveredItem by derivedStateOf {
        draggingItem?.let { dragging ->
            lazyListState.layoutInfo.visibleItemsInfo
                .filter { it.key != dragging.key }
                .firstOrNull { item ->
                    (dragging.offset + offsetY).toInt() in (item.offset..item.offset + item.size)
                }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            state = lazyListState
        ) {
            itemsIndexed(items) { index, item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .zIndex(if (draggingItem?.index == index) 1f else 0f)
                        .graphicsLayer {
                            draggingItem?.let { dragging ->
                                if (dragging.index == index) {
                                    translationY = offsetY
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        Modifier
                            .graphicsLayer {
                                if (index == draggingItem?.index) {
                                    alpha = .5f
                                    scaleX = .9f
                                }
                            }
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            onItemContent(item)
                        }
                    }
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .padding(8.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(onDragStart = {
                                    draggingItem =
                                        lazyListState.layoutInfo.visibleItemsInfo.getOrNull(
                                            index
                                        )
                                }, onDragEnd = {
                                    draggingItem = null
                                    offsetY = 0f
                                }, onDragCancel = {
                                    offsetY = 0f
                                    draggingItem = null
                                }) { change, (_, y) ->
                                    change.consumeAllChanges()
                                    offsetY += y
                                    draggingItem?.let { dragging ->
                                        hoveredItem?.let { hovered ->
                                            onMove(
                                                dragging.index,
                                                hovered.index
                                            )
                                            draggingItem = hovered
                                            offsetY += ((dragging.index - hovered.index) * dragging.size)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Reorder,
                            "reorder list"
                        )
                    }
                }
            }
        }
    }
}