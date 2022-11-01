package com.numq.composesnippets.components.reorderable.row

import androidx.compose.foundation.gestures.animateScrollBy
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
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun <T : Any> ReorderableRow(
    modifier: Modifier,
    data: List<T>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    itemContent: @Composable (T) -> Unit
) {

    val listState = rememberLazyListState()

    var offsetX by remember { mutableStateOf(0f) }

    var draggableItem by remember { mutableStateOf<LazyListItemInfo?>(null) }

    val overscrollStart by derivedStateOf {
        draggableItem?.let { draggable ->
            val anchor = draggable.offset + offsetX
            if (anchor < 0) -draggable.size else null
        }
    }

    val overscrollEnd by derivedStateOf {
        draggableItem?.let { draggable ->
            val anchor = draggable.offset + draggable.size + offsetX
            if (anchor > listState.layoutInfo.viewportEndOffset) draggable.size else null
        }
    }

    LaunchedEffect(overscrollStart, overscrollEnd) {
        overscrollStart?.let {
            listState.animateScrollBy(it.toFloat())
        }
        overscrollEnd?.let {
            listState.animateScrollBy(it.toFloat())
        }
        delay(500L)
    }

    val overlappedItem by derivedStateOf {
        draggableItem?.let { draggable ->
            val offset = draggable.offset
            val size = draggable.size
            listState.layoutInfo.visibleItemsInfo
                .filterNot { it.index == draggable.index }
                .firstOrNull { item ->
                    (offset + size / 2 + offsetX).roundToInt() in (item.offset..item.offset + item.size)
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