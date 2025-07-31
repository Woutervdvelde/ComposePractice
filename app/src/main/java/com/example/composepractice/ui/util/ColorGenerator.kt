package com.example.composepractice.ui.util

import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import androidx.compose.ui.graphics.Color as ComposeColor

object ColorGenerator {

    /**
     * Generated a color based on provided text.
     * This will always return the same color for the same text.
     */
    fun generateBackgroundColor(text: String): ComposeColor {
        val hash = text.hashCode()
        val hue = (abs(hash) % 360).toFloat()

        val saturation = .5f
        val lightness = .45f

        val hsl = floatArrayOf(hue, saturation, lightness)
        return ComposeColor(Color.HSVToColor(hsl))
    }

    fun getForegroundColor(backgroundColor: ComposeColor): ComposeColor {
        val color = backgroundColor.toArgb()
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0

        val luminance = (.2126 * r + .7152 * g + .0722 * b).toFloat()

        return if (luminance < .5) ComposeColor.White
            else ComposeColor.Black
    }
}