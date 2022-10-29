package com.numq.composesnippets.components.drawing

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun DrawingScreen() {

    val coroutineScope = rememberCoroutineScope()

    val darkTheme = isSystemInDarkTheme()

    val defaultColor = if (darkTheme) Color.White else Color.Black

    val defaultBackgroundColor = if (darkTheme) Color.Black else Color.White

    val currentPoints = remember { mutableStateListOf<Offset>() }

    val currentBackStack = remember { mutableStateListOf<DrawingTile>() }

    val undoBackStack = remember { mutableStateListOf<DrawingTile>() }

    val (minBrushSize, maxBrushSize) = Pair(4f, 100f)

    var currentBrushSize by remember { mutableStateOf(minBrushSize) }

    var brushSizeState by remember { mutableStateOf(BrushSizeState.NONE) }

    var currentColor by remember { mutableStateOf(defaultColor) }

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
            .background(defaultBackgroundColor)
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
                    strokeWidth = brushSize,
                    cap = StrokeCap.Round
                )
            }
            drawPoints(
                currentPoints,
                pointMode = PointMode.Polygon,
                color = currentColor,
                strokeWidth = currentBrushSize,
                cap = StrokeCap.Round
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
                            if (brushSizeState != BrushSizeState.NONE) {
                                Divider(
                                    thickness = currentBrushSize.dp,
                                    color = currentColor
                                )
                            }
                            Row(
                                Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (brushSizeState == BrushSizeState.INCREASING) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        "increase color value",
                                        tint = defaultColor
                                    )
                                }
                                Card(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .pointerInput(Unit) {
                                            detectDragGestures(onDragCancel = {
                                                brushSizeState = BrushSizeState.NONE
                                            }, onDragEnd = {
                                                brushSizeState = BrushSizeState.NONE
                                            }) { change, _ ->
                                                change.consumeAllChanges()
                                                val x = change.position.x
                                                Log.e(javaClass.simpleName, x.toString())
                                                when {
                                                    x > 0 -> brushSizeState =
                                                        BrushSizeState.INCREASING
                                                    x < 0 -> brushSizeState =
                                                        BrushSizeState.DECREASING
                                                    else -> Unit
                                                }
                                                coroutineScope.launch {
                                                    currentBrushSize =
                                                        currentBrushSize
                                                            .plus(sign(x))
                                                            .coerceIn(minBrushSize, maxBrushSize)
                                                    delay(500L)
                                                }
                                            }
                                        }, backgroundColor = currentColor, shape = CircleShape
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "${currentBrushSize.roundToInt()}.px",
                                            color = if (currentColor.luminance() < 0) Color.White else Color.Black
                                        )
                                    }
                                }
                                if (brushSizeState == BrushSizeState.DECREASING) {
                                    Icon(
                                        Icons.Rounded.Remove,
                                        "decrease color value",
                                        tint = defaultColor
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