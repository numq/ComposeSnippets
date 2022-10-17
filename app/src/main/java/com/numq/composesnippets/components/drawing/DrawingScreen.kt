package com.numq.composesnippets.components.drawing

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun DrawingScreen() {

    val currentBackStack = remember { mutableStateListOf<List<Offset>>(emptyList()) }

    val undoBackStack = remember { mutableStateListOf<List<Offset>>(emptyList()) }

    val currentPoints = remember { mutableStateListOf<Offset>() }

    val (currentColor, setCurrentColor) = remember { mutableStateOf(Color.Green) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(onDragEnd = {
                        currentBackStack.add(currentPoints)
                        currentPoints.clear()
                    }) { change, dragAmount ->
                        change.consumeAllChanges()
                        currentPoints.add(dragAmount)
                    }
                }) {
            currentBackStack.forEach { points ->
                drawPoints(points, pointMode = PointMode.Polygon, color = currentColor)
            }
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