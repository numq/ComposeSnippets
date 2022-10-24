package com.numq.composesnippets.components.reorderable.column

import android.util.Log
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.numq.composesnippets.components.saver.rememberMutableStateListOf
import java.util.*

@Composable
fun ReorderableColumnScreen() {
    val items = rememberMutableStateListOf(
        *arrayOfNulls<String>(10)
            .mapIndexed { index, _ -> "$index" }
            .toTypedArray()
    )
    ReorderableColumn(items, onMove = { fromIndex, toIndex ->
        Log.e(
            "ReordableColumn",
            "replaced from $fromIndex with text ${items[fromIndex]} to $toIndex with text ${items[toIndex]}"
        )
        Collections.swap(items, fromIndex, toIndex)
    }) {
        Text(it)
    }
}