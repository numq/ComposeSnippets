package com.numq.composesnippets.components.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class DrawingTile(val color: Color, val brushSize: Float, val points: List<Offset>)