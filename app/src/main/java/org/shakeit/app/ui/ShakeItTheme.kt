package org.shakeit.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple = Color(0xFF7658F6)
@Composable fun ShakeItTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = lightColorScheme(primary = Purple, background = Color(0xFFF7F5FB), surface = Color.White), content = content) }
