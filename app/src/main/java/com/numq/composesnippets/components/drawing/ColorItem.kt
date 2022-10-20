package com.numq.composesnippets.components.drawing

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ColorItem(color: Color, onClick: () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    IconButton(onClick) {
        Icon(
            Icons.Rounded.Circle,
            "background circle",
            tint = if (darkTheme) Color.White else Color.Black
        )
        Icon(
            Icons.Rounded.Circle,
            "foreground circle",
            tint = color,
            modifier = Modifier.graphicsLayer {
                scaleX = .9f
                scaleY = .9f
            })
    }
}