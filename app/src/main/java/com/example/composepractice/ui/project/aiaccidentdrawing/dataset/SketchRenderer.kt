package com.example.composepractice.ui.project.aiaccidentdrawing.dataset

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.DrawType
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.DrawnStroke

/**
 * Renders strokes with the same visual parameters as [DrawingScreen]'s live Compose canvas
 * (white background, 8px round-capped/joined strokes, red/blue/black per [DrawType]), using
 * plain android.graphics so it can run off-device under Robolectric for dataset generation.
 */
internal fun renderSketch(
    strokes: List<DrawnStroke>,
    width: Int = 512,
    height: Int = 512,
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    strokes.forEach { stroke ->
        val points = stroke.points
        val first = points.firstOrNull() ?: return@forEach
        val path = Path().apply {
            moveTo(first.x, first.y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        paint.color = stroke.drawType.toAndroidColor()
        canvas.drawPath(path, paint)
    }

    return bitmap
}

private fun DrawType.toAndroidColor(): Int = when (this) {
    DrawType.A -> Color.RED
    DrawType.B -> Color.BLUE
    DrawType.Road -> Color.BLACK
}
