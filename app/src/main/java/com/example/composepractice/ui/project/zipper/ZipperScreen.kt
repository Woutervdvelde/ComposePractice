package com.example.composepractice.ui.project.zipper

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.composepractice.R
import com.example.composepractice.ui.theme.ComposePracticeTheme
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@Composable
fun ZipperScreen() {
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    var zipperY by remember { mutableFloatStateOf(1f) }
    val visualZipperY = remember { Animatable(zipperY) }

    val zipperAreaHeight = 500.dp
    val denimTexture = BitmapFactory
        .decodeResource(resources, R.drawable.denim_texture)
        .asImageBitmap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        scope.launch {
                            zipperY = max(zipperY + dragAmount, 1f)
                            visualZipperY.animateTo(
                                targetValue = zipperY,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }
                )
            }
            .drawWithContent {
                val strokeHeight = 1 // Defines the definition of each stroke height
                val zipAreaHeightPx = zipperAreaHeight.toPx()
                val currentZipperY = visualZipperY.value
                val halfWidth = size.width / 2f

                val controlPointYMultiplier = mapValue(
                    inputValue = currentZipperY,
                    inputStart = 0f,
                    inputEnd = size.height,
                    outputStart = 1f,
                    outputEnd = 4f
                )
                val controlPointY = min(zipAreaHeightPx / controlPointYMultiplier, zipAreaHeightPx)

                val leftZipper = Path().apply {
                    moveTo(x = 0f, y = currentZipperY - zipAreaHeightPx)
                    cubicTo(
                        0f, currentZipperY - (zipAreaHeightPx / 2f),
                        halfWidth, currentZipperY - controlPointY,
                        halfWidth, currentZipperY
                    )
                    lineTo(halfWidth, currentZipperY + size.height)
                }

                val rightZipper = Path().apply {
                    moveTo(x = size.width, y = currentZipperY - zipAreaHeightPx)
                    cubicTo(
                        size.width, currentZipperY - (zipAreaHeightPx / 2f),
                        halfWidth, currentZipperY - controlPointY,
                        halfWidth, currentZipperY
                    )
                    lineTo(halfWidth, currentZipperY + size.height)
                }

                val pathMeasure = PathMeasure().apply {
                    setPath(path = rightZipper, forceClosed = false)
                }
                Log.e("MEOW", "height: ${size.height} - line length: ${pathMeasure.length}")
                drawContent()

                drawIntoCanvas { canvas ->
                    // Zipper
                    drawCircle(
                        color = Color.Black,
                        radius = 100f,
                        center = Offset(x = halfWidth, y = visualZipperY.value)
                    )

                    // Control point
                    drawCircle(
                        color = Color.Blue,
                        radius = 50f,
                        center = Offset(
                            x = halfWidth,
                            y = currentZipperY - (zipAreaHeightPx / controlPointYMultiplier)
                        )
                    )

                    for (i in 0..size.height.toInt() / strokeHeight) {
                        val y = i * strokeHeight
                        val position = mapValue(
                            inputValue = y.toFloat() + zipAreaHeightPx,
                            inputStart = 0f,
                            inputEnd = size.height + zipAreaHeightPx,
                            outputStart = 0f,
                            outputEnd = pathMeasure.length
                        )

                        val x = pathMeasure
                            .getPosition(position).x // Current x offset along path
                        Log.e("MEOW", "input: ${y.toFloat() + zipAreaHeightPx} - inputEnd ${size.height + zipAreaHeightPx} - outputEnd ${pathMeasure.length} - x ${x}")


                        val srcOffset = IntOffset(
                            x = halfWidth.toInt(),
                            y = y
                        )
                        val srcSize = IntSize(
                            width = denimTexture.width,
                            height = strokeHeight
                        )
                        val dstSize = srcSize
                        val dstOffset = IntOffset(
                            x = x.toInt(),
                            y = y
                        )

                        drawImage(
                            image = denimTexture,
                            srcSize = srcSize,
                            srcOffset = srcOffset,
                            dstOffset = dstOffset,
                            dstSize = dstSize
                        )
                    }

                    drawPath(
                        path = leftZipper,
                        color = Color.Red,
                        style = Stroke(
                            width = 2.dp.toPx()
                        )
                    )
                    drawPath(
                        path = rightZipper,
                        color = Color.Red,
                        style = Stroke(
                            width = 2.dp.toPx()
                        )
                    )
                }

            }
    ) {

    }
}

fun mapValue(inputValue: Float, inputStart: Float, inputEnd: Float, outputStart: Float, outputEnd: Float): Float {
    val ratio = (inputValue - inputStart) / (inputEnd - inputStart)
    return outputStart + ratio * (outputEnd - outputStart)
}

@Preview
@Composable
private fun ZipperScreenPreview() {
    ComposePracticeTheme {
        ZipperScreen()
    }
}