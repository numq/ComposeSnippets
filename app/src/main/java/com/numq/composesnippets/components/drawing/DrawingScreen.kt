package com.numq.composesnippets.components.drawing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material.icons.rounded.Undo
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun DrawingScreen() {

    val darkTheme = isSystemInDarkTheme()

    val coroutineScope = rememberCoroutineScope()

    val currentPoints = remember { mutableStateListOf<Offset>() }

    val currentBackStack = remember { mutableStateListOf<DrawingTile>() }

    val undoBackStack = remember { mutableStateListOf<DrawingTile>() }

    val (minBrushSize, maxBrushSize) = Pair(4f, 100f)

    var currentBrushSize by remember { mutableStateOf(minBrushSize) }

    var isBrushSizeChanging by remember { mutableStateOf(false) }

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

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(if (darkTheme) Color.Black else Color.White)
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = {
                    currentBackStack.add(
                        DrawingTile(
                            currentColor,
                            currentBrushSize,
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
            currentBackStack.forEach { (color, brushSize, points) ->
                drawPoints(
                    points,
                    pointMode = PointMode.Polygon,
                    color = color,
                    strokeWidth = brushSize
                )
            }
            drawPoints(
                currentPoints,
                pointMode = PointMode.Polygon,
                color = currentColor,
                strokeWidth = currentBrushSize
            )
        }
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (colorPickerVisible) {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isBrushSizeChanging) {
                                    Divider(
                                        Modifier.fillMaxWidth(),
                                        thickness = currentBrushSize.dp,
                                        color = currentColor
                                    )
                                }
                                Card {
                                    Text(
                                        "${currentBrushSize.roundToInt()}.px",
                                        Modifier.pointerInput(Unit) {
                                            detectDragGestures(onDragStart = {
                                                isBrushSizeChanging = true
                                            }, onDragCancel = {
                                                isBrushSizeChanging = false
                                            }, onDragEnd = {
                                                isBrushSizeChanging = false
                                            }) { change, (x, y) ->
                                                change.consumeAllChanges()
                                                coroutineScope.launch {
                                                    currentBrushSize =
                                                        currentBrushSize.plus(sign(x) * 1)
                                                            .coerceIn(minBrushSize, maxBrushSize)
                                                    delay(1000L)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            Row(
                                Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                colors.filter { it != currentColor }.forEach { color ->
                                    ColorItem(color) {
                                        currentColor = color
                                        setColorPickerVisible(false)
                                    }
                                }
                            }
                        }
                    }
                    Card {
                        Row(
                            Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                currentBackStack.clear()
                            }) {
                                Icon(Icons.Rounded.Clear, "remove content")
                            }
                            IconButton(onClick = {
                                if (currentBackStack.isNotEmpty()) {
                                    undoBackStack.add(currentBackStack.last())
                                    currentBackStack.removeLast()
                                }
                            }) {
                                Icon(Icons.Rounded.Undo, "undo")
                            }
                            IconButton(onClick = {
                                if (undoBackStack.isNotEmpty()) {
                                    currentBackStack.add(undoBackStack.last())
                                    undoBackStack.removeLast()
                                }
                            }) {
                                Icon(Icons.Rounded.Redo, "redo")
                            }
                            ColorItem(currentColor) {
                                setColorPickerVisible(!colorPickerVisible)
                            }
                        }
                    }
                }
            }
        }
    }
}