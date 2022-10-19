package com.numq.composesnippets.components.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorItem(color: Color, onClick: () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (darkTheme) Color.White else Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(color)
                .clickable {
                    onClick()
                }
        )
    }
}