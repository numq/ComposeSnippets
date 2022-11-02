package com.numq.composesnippets.components.reorderable.modifier

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import kotlin.math.roundToInt

class ReorderableState internal constructor(
    val listType: ListType,
    val listState: LazyListState,
    val onMove: ((fromIndex: Int, toIndex: Int) -> Unit)
) {
    var offsetX by mutableStateOf(0f)
    var offsetY by mutableStateOf(0f)
    var draggableItem by mutableStateOf<LazyListItemInfo?>(null)
    val dropIndex by derivedStateOf {
        draggableItem?.let { draggable ->
            val anchor = (draggable.offset + draggable.size / 2 + offsetX + offsetY).roundToInt()
            listState.layoutInfo.visibleItemsInfo
                .filterNot { it.index == draggableItem?.index }
                .firstOrNull { item ->
                    anchor in (item.offset..item.offset + item.size)
                }?.index
        }
    }
    val overscrollValue by derivedStateOf {
        draggableItem?.let {
            val offset = if (listType is ListType.Row) offsetX else offsetY
            when {
                offset > 0 -> (it.offset + it.size + offset - listState.layoutInfo.viewportEndOffset).takeIf { diff -> diff > 0 }
                offset < 0 -> (it.offset + offset - listState.layoutInfo.viewportStartOffset).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}

@Composable
fun rememberReorderableRowState(
    key: Any? = null,
    listState: LazyListState = rememberLazyListState(),
    onMove: ((fromIndex: Int, toIndex: Int) -> Unit)
) = remember(key) { ReorderableState(ListType.Row, listState, onMove) }

@Composable
fun rememberReorderableColumnState(
    key: Any? = null,
    listState: LazyListState = rememberLazyListState(),
    onMove: ((fromIndex: Int, toIndex: Int) -> Unit)
) = remember(key) { ReorderableState(ListType.Column, listState, onMove) }

@Composable
fun rememberReorderableGridState(
    key: Any? = null,
    listState: LazyListState = rememberLazyListState(),
    onMove: ((fromIndex: Int, toIndex: Int) -> Unit)
) = remember(key) { ReorderableState(ListType.Grid, listState, onMove) }