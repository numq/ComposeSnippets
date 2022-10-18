package com.numq.composesnippets.components.drawing

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun DrawingScreen() {


    val currentPoints = remember { mutableStateListOf<Offset>() }

    val currentBackStack = remember { mutableStateListOf<List<Offset>>() }

    val undoBackStack = remember { mutableStateListOf<List<Offset>>() }

    LaunchedEffect(currentBackStack) {
        Log.e(javaClass.simpleName, currentBackStack.joinToString("\n"))
    }

    val (currentColor, setCurrentColor) = remember { mutableStateOf(Color.Green) }

    Box(
        Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = {
                    currentBackStack.add(listOf(*currentPoints.toTypedArray()))
                    currentPoints.clear()
                }) { change, _ ->
                    change.consumeAllChanges()
                    currentPoints.add(change.position)
                }
            }
        ) {
            currentBackStack.forEach { points ->
                drawPoints(
                    points,
                    pointMode = PointMode.Polygon,
                    color = currentColor
                )
            }
            drawPoints(
                currentPoints,
                pointMode = PointMode.Polygon,
                color = currentColor
            )
        }
        Card {
            Row(Modifier.padding(4.dp)) {
                IconButton(onClick = {
                    if (currentBackStack.isNotEmpty()) {
                        undoBackStack.add(currentBackStack.last())
                        currentBackStack.removeLast()
                    }
                }) {
                    Icon(Icons.Rounded.ArrowBack, "undo")
                }
                IconButton(onClick = {
                    currentBackStack.clear()
                }) {
                    Icon(Icons.Rounded.Clear, "remove content")
                }
                IconButton(onClick = {
                    if (undoBackStack.isNotEmpty()) {
                        currentBackStack.add(undoBackStack.last())
                        undoBackStack.removeLast()
                    }
                }) {
                    Icon(Icons.Rounded.ArrowForward, "redo")
                }
            }
        }
    }
}