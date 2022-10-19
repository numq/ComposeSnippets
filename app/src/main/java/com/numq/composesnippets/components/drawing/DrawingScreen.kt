package com.numq.composesnippets.components.drawing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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

    val darkTheme = isSystemInDarkTheme()

    val currentPoints = remember { mutableStateListOf<Offset>() }

    val currentBackStack = remember { mutableStateListOf<DrawingTile>() }

    val undoBackStack = remember { mutableStateListOf<DrawingTile>() }

    var currentColor by remember { mutableStateOf(if (darkTheme) Color.White else Color.Black) }

    val colors = listOf(Color.Black, Color.White).sortedBy { darkTheme } + listOf(
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Cyan,
        Color.Magenta
    )

    val (colorPickerVisible, setColorPickerVisible) = remember { mutableStateOf(false) }

    BackHandler(colorPickerVisible) {
        setColorPickerVisible(false)
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) { if (colorPickerVisible) setColorPickerVisible(false) },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(if (darkTheme) Color.Black else Color.White)
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = {
                    currentBackStack.add(
                        DrawingTile(
                            currentColor,
                            currentPoints.toList()
                        )
                    )
                    currentPoints.clear()
                }) { change, _ ->
                    change.consumeAllChanges()
                    currentPoints.add(change.position)
                }
            }
        ) {
            currentBackStack.forEach { (color, points) ->
                drawPoints(
                    points,
                    pointMode = PointMode.Polygon,
                    color = color
                )
            }
            drawPoints(
                currentPoints,
                pointMode = PointMode.Polygon,
                color = currentColor
            )
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Card {
                Row(
                    Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (colorPickerVisible) {
                        colors.filter { it != currentColor }.forEach { color ->
                            ColorItem(color) {
                                currentColor = color
                                setColorPickerVisible(false)
                            }
                        }
                    }
                    ColorItem(currentColor) {
                        setColorPickerVisible(!colorPickerVisible)
                    }
                }
            }
        }
    }
}