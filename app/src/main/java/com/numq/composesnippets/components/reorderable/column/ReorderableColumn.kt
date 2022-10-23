package com.numq.composesnippets.components.reorderable.column

import android.util.Log
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
fun <T : Any> ReorderableColumn(
    items: MutableList<T>,
    onMove: (Int, Int) -> Unit,
    onItemContent: @Composable (T) -> Unit
) {

    val lazyListState = rememberLazyListState()

    var draggingItem by remember { mutableStateOf<LazyListItemInfo?>(null) }

    LaunchedEffect(draggingItem) {
        Log.e(javaClass.simpleName, draggingItem?.index.toString())
    }

    var offsetY by remember { mutableStateOf(0f) }

    val dragDirection by derivedStateOf {
        when {
            offsetY < 0 -> ColumnDragDirection.UP
            offsetY > 0 -> ColumnDragDirection.DOWN
            else -> ColumnDragDirection.NONE
        }
    }

    val hoveredItem by derivedStateOf {
        draggingItem?.let { dragging ->
            lazyListState.layoutInfo.visibleItemsInfo
                .filter { it.key != dragging.key }
                .firstOrNull { item ->
                    when (dragDirection) {
                        ColumnDragDirection.UP -> (dragging.offset + dragging.size + offsetY).toInt() in (item.offset..item.offset + item.size)
                        ColumnDragDirection.DOWN -> (dragging.offset + offsetY).toInt() in (item.offset..item.offset + item.size)
                        else -> false
                    }
                }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val topReached by derivedStateOf {
            draggingItem?.let { dragging ->
                dragging.offset + offsetY < 0 && dragging.index > 0
            } ?: false
        }
        LaunchedEffect(topReached) {
            if (topReached) {
                draggingItem?.let {
                    lazyListState.animateScrollToItem(it.index - 1)
                    Log.e(javaClass.simpleName, "topReached")
                }
            }
        }
        val bottomReached by derivedStateOf {
            draggingItem?.let { dragging ->
                dragging.offset + dragging.size + offsetY > maxHeight.value && dragging.index < lazyListState.layoutInfo.totalItemsCount
            } ?: false
        }
        LaunchedEffect(bottomReached) {
            if (bottomReached) {
                draggingItem?.let {
                    lazyListState.animateScrollToItem(it.index + 1)
                    Log.e(javaClass.simpleName, "bottomReached")
                }
            }
        }
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            state = lazyListState
        ) {
            itemsIndexed(items, key = { _, i -> i }) { index, item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(maxHeight / 5)
                        .zIndex(if (draggingItem?.index == index) 1f else 0f)
                        .graphicsLayer {
                            if (draggingItem?.index == index) {
                                translationY = offsetY
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        Modifier
                            .graphicsLayer {
                                if (draggingItem?.index == index) {
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
                                    draggingItem = lazyListState.layoutInfo.visibleItemsInfo.find { it.key == item }
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
                                            draggingItem = hoveredItem
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