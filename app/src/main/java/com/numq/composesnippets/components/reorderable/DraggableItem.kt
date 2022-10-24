package com.numq.composesnippets.components.reorderable

import androidx.compose.foundation.lazy.LazyListItemInfo

internal data class DraggableItem(val index: Int, val itemInfo: LazyListItemInfo)