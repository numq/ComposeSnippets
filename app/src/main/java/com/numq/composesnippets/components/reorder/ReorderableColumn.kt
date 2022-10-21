package com.numq.composesnippets.components.reorder

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
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
    items: List<T>,
    onMove: (Int, Int) -> Unit,
    onItemContent: @Composable (T) -> Unit
) {

    val lazyListState = rememberLazyListState()

    var offsetY by remember { mutableStateOf(0f) }

    var draggingItem by remember { mutableStateOf<LazyListItemInfo?>(null) }

    val hoveredItem by derivedStateOf {
        draggingItem?.let { draggingItem ->
            val draggingAnchor = draggingItem.offset + offsetY + (draggingItem.size / 2f)
            lazyListState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.offset <= draggingAnchor && draggingAnchor <= it.offset + it.size }
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
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .padding(4.dp)
                        .zIndex(if (draggingItem?.index == index) 1f else 0f)
                        .graphicsLayer {
                            if (draggingItem?.index == index) {
                                translationY = offsetY
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(onDragStart = {
                                draggingItem =
                                    lazyListState.layoutInfo.visibleItemsInfo.getOrNull(index)
                            }, onDragEnd = {
                                draggingItem?.let { draggingItem ->
                                    hoveredItem?.let { hoveredItem ->
                                        onMove(
                                            draggingItem.index,
                                            hoveredItem.index
                                        )
                                    }
                                }
                                draggingItem = null
                                offsetY = 0f
                            }, onDragCancel = {
                                offsetY = 0f
                                draggingItem = null
                            }) { change, (_, y) ->
                                change.consumeAllChanges()
                                offsetY += y
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(Modifier.weight(1f)) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            onItemContent(item)
                        }
                    }
                }
            }
        }
    }
}