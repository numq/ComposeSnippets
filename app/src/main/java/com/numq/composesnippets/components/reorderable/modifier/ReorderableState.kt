package com.numq.composesnippets.components.reorderable.modifier

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import kotlin.math.roundToInt

class ReorderableState internal constructor(
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
}

@Composable
fun rememberReorderableListState(
    key: Any? = null,
    listState: LazyListState = rememberLazyListState(),
    onMove: ((fromIndex: Int, toIndex: Int) -> Unit)
) = remember(key) { ReorderableState(listState, onMove) }