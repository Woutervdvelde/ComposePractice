package com.example.composepractice.ui.project.math

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MathScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.safeDrawingPadding(),
    ) {
        CosCanvas(modifier = Modifier.size(300.dp))
        SinCanvas(modifier = Modifier.size(300.dp))
        RandomishWaveCanvas(modifier = Modifier.size(300.dp))
        WigglyBox(modifier = Modifier.size(300.dp))
    }
}

@Composable
fun CosCanvas(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.background(Color.Black),
    ) {
        val path = Path().apply {
            val centerY = size.height / 2f
            for (x in 0..size.width.toInt()) {
                val progress = x.toFloat() / size.width
                val amplitude = size.height / 2f
                val angle = progress * (2f * Math.PI)
                val y = centerY + (cos(angle) * amplitude)

                if (x == 0) moveTo(x = 0f, y = y.toFloat())
                else lineTo(x.toFloat(), y.toFloat())
            }
        }

        drawPath(path = path, color = Color.White, style = Stroke())
    }
}

@Composable
fun SinCanvas(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.background(Color.Black),
    ) {
        val waveLength = 100.dp.toPx()
        val frequency = size.width / waveLength

        val path = Path().apply {
            val centerY = size.height / 2f
            for (x in 0..size.width.toInt()) {
                val progress = x.toFloat() / size.width
                val amplitude = size.height / 2f
                val angle = progress * (2f * Math.PI) * frequency
                val y = centerY + (sin(angle) * amplitude)

                if (x == 0) moveTo(x = 0f, y = y.toFloat())
                else lineTo(x.toFloat(), y.toFloat())
            }
        }

        drawPath(path = path, color = Color.White, style = Stroke())
    }
}

@Composable
fun RandomishWaveCanvas(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.background(Color.Black),
    ) {
        val waveLength = 200.dp.toPx()
        val maxAmplitude = 20.dp.toPx()
        val frequency = size.width / waveLength
        val centerY = size.height / 2f

        val aBase = maxAmplitude * .7f
        val aNoise1 = maxAmplitude * .15f
        val aNoise2 = maxAmplitude * .1f
        val aNoise3 = maxAmplitude * .05f

        val path = Path().apply {
            for (x in 0..size.width.toInt()) {
                val progress = x.toFloat() / size.width

                val angle = progress * (2f * Math.PI.toFloat()) * frequency
                val baseWave = sin(angle) * aBase
                val noise1 = sin(angle * 2.71f) * aNoise1
                val noise2 = cos(angle * 7.13f) * aNoise2
                val noise3 = sin(angle * 19.41f) * aNoise3

                val totalDisplacement = baseWave + noise1 + noise2 + noise3
                val y = centerY + totalDisplacement

                if (x == 0) moveTo(x = 0f, y = y)
                else lineTo(x.toFloat(), y)
            }
        }

        drawPath(path = path, color = Color.White, style = Stroke())
    }
}

@Composable
fun WigglyBox(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.background(Color.Black),
    ) {
        drawWigglyBox(
            rect = Rect(
                offset = Offset(8.dp.toPx(), 8.dp.toPx()),
                size = Size(size.width - (16.dp.toPx()), size.height - (16.dp.toPx())),
            ),
            waveLength = 50.dp,
            amplitude = 2.dp,
            style = Fill
        )
    }
}

private fun calculateDisplacement(
    progress: Float,
    amplitude: Float,
    frequency: Float,
): Float {
    val aBase = amplitude * .5f
    val aNoise1 = amplitude * .35f
    val aNoise2 = amplitude * .1f
    val aNoise3 = amplitude * .05f

    val angle = progress * (2f * Math.PI.toFloat()) * frequency
    val baseWave = sin(angle) * aBase
    val noise1 = sin(angle * 2.71f) * aNoise1
    val noise2 = cos(angle * 7.13f) * aNoise2
    val noise3 = sin(angle * 19.41f) * aNoise3

    return baseWave + noise1 + noise2 + noise3
}

fun DrawScope.drawWigglyBox(
    rect: Rect,
    waveLength: Dp = 200.dp,
    amplitude: Dp = 20.dp,
    style: DrawStyle = Stroke()
) {
    val maxAmplitude = amplitude.toPx()
    val frequency = size.width / waveLength.toPx()

    val path = Path().apply {
        var x = 0f
        var y = 0f

        for (currentX in 0..rect.width.toInt()) {
            x = currentX.toFloat()
            val progress = x / rect.width
            val displacement = calculateDisplacement(
                progress = progress,
                amplitude = maxAmplitude,
                frequency = frequency,
            )

            y = 0f + displacement

            if (x == 0f) moveTo(x = 0f, y = y)
            else lineTo(x, y)
        }

        var lastX = x
        var lastY = y
        for (currentY in lastY.toInt()..rect.height.toInt()) {
            y = currentY.toFloat()
            val progress = y / rect.height
            val displacement = calculateDisplacement(
                progress = progress,
                amplitude = maxAmplitude,
                frequency = frequency,
            )

            x = lastX + displacement
            lineTo(x, y)
        }

        lastX = x
        lastY = y
        for (currentX in 0..lastX.toInt()) {
            x = lastX - currentX.toFloat()
            val progress = x / rect.width
            val displacement = calculateDisplacement(
                progress = progress,
                amplitude = maxAmplitude,
                frequency = frequency,
            )

            y = lastY + displacement
            lineTo(x, y)
        }

        lastX = x
        lastY = y
        for (currentY in 0..lastY.toInt()) {
            y = lastY - currentY.toFloat()
            val progress = y / rect.height
            val displacement = calculateDisplacement(
                progress = progress,
                amplitude = maxAmplitude,
                frequency = frequency,
            )

            x = lastX + displacement
            lineTo(x, y)
        }
    }

    translate(left = rect.left, top = rect.top) {
        drawPath(path = path, color = Color.White, style = style)
    }
}

@Preview(heightDp = 3000, widthDp = 1000)
@Composable
private fun MathScreenPreview() {
    MathScreen()
}