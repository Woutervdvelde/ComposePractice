package com.example.composepractice.ui.project.zipper

import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.VertexMode
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
    var zipperY by remember { mutableFloatStateOf(200f) }
    val visualZipperY = remember { Animatable(zipperY) }

    val zipperAreaHeight = 500.dp
    val denimTexture = remember {
        BitmapFactory.decodeResource(resources, R.drawable.denim_texture).asImageBitmap()
    }

    BoxWithConstraints {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .drawWithContent {
                    val zipAreaHeightPx = zipperAreaHeight.toPx()
                    val currentZipperY = visualZipperY.value
                    val halfWidth = size.width / 2f

                    val controlPointYMultiplier = mapValue(
                        inputValue = currentZipperY,
                        inputStart = 0f,
                        inputEnd = size.height,
                        outputStart = .5f,
                        outputEnd = 5f
                    )
                    val controlPointY =
                        min(zipAreaHeightPx / controlPointYMultiplier, zipAreaHeightPx)

                    val leftBezierPoints = sampleBezierPoints(
                        p0 = Offset(0f, currentZipperY - zipAreaHeightPx),
                        p1 = Offset(0f, currentZipperY - zipAreaHeightPx / 2),
                        p2 = Offset(halfWidth, currentZipperY - controlPointY),
                        p3 = Offset(halfWidth, currentZipperY),
                        samples = 80
                    )
                    val rightBezierPoints = sampleBezierPoints(
                        p0 = Offset(size.width, currentZipperY - zipAreaHeightPx),
                        p1 = Offset(size.width, currentZipperY - zipAreaHeightPx / 2),
                        p2 = Offset(halfWidth, currentZipperY - controlPointY),
                        p3 = Offset(halfWidth, currentZipperY),
                        samples = 80
                    )

                    val shader = ImageShader(denimTexture, TileMode.Repeated, TileMode.Repeated)
                    val matrix = Matrix().apply { setScale(0.001f, 0.0002f) }
                    shader.setLocalMatrix(matrix)

                    val paint = Paint().apply {
                        asFrameworkPaint().shader = shader
                    }

                    // Build and draw fabric meshes
                    val rows = 40
                    val leftFabric = buildMesh(
                        leftX = { -200f },
                        rightX = { y -> xForY(leftBezierPoints, y) },
                        height = size.height,
                        rows = rows
                    )
                    val rightFabric = buildMesh(
                        leftX = { y -> xForY(rightBezierPoints, y) },
                        rightX = { size.width + 200f },
                        height = size.height,
                        rows = rows
                    )

                    drawContent()

                    drawIntoCanvas { canvas ->
                        canvas.drawVertices(
                            vertices = leftFabric,
                            paint = paint,
                            blendMode = BlendMode.Screen
                        )

                        canvas.drawVertices(
                            vertices = rightFabric,
                            paint = paint,
                            blendMode = BlendMode.Screen
                        )
                    }
                }
        ) {}

        Zipper(
            onDrag = { dragAmount ->
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
            },
            width = 30.dp,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (maxWidth.roundToPx() - 30.dp.roundToPx()) / 2,
                        y = visualZipperY.value.toInt()
                    )
                }
        )
    }
}

@Composable
private fun Zipper(
    onDrag: (Float) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 30.dp,
) {
    Box(
        modifier = modifier
            .size(width)
            .background(Color.Black, CircleShape)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    onDrag(dragAmount)
                }
            }
    )
}

private fun buildMesh(
    leftX: (Float) -> Float,
    rightX: (Float) -> Float,
    height: Float,
    rows: Int
): Vertices {
    val positions = mutableListOf<Offset>()
    val tex = mutableListOf<Offset>()
    val colors = mutableListOf<Color>()
    val indices = mutableListOf<Int>()

    for (i in 0..rows) {
        val y = i * (height / rows)
        positions += Offset(leftX(y), y)
        positions += Offset(rightX(y), y)

        val v = y / height
        tex += Offset(0f, v)
        tex += Offset(1f, v)
        colors += Color.White
        colors += Color.White

        if (i < rows) {
            val idx = i * 2
            indices += listOf(idx, idx + 1, idx + 2, idx + 1, idx + 3, idx + 2)
        }
    }

    return Vertices(
        vertexMode = VertexMode.Triangles,
        positions = positions,
        textureCoordinates = tex,
        colors = colors,
        indices = indices
    )
}

private fun sampleBezierPoints(
    p0: Offset, p1: Offset, p2: Offset, p3: Offset, samples: Int
): List<Offset> {
    return List(samples + 1) { i ->
        val t = i / samples.toFloat()
        val x = cubicBezier(p0.x, p1.x, p2.x, p3.x, t)
        val y = cubicBezier(p0.y, p1.y, p2.y, p3.y, t)
        Offset(x, y)
    }.sortedBy { it.y }
}

private fun cubicBezier(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
    val u = 1 - t
    return u * u * u * a + 3 * u * u * t * b + 3 * u * t * t * c + t * t * t * d
}

private fun xForY(points: List<Offset>, y: Float): Float {
    val lower = points.lastOrNull { it.y <= y } ?: points.first()
    val upper = points.firstOrNull { it.y >= y } ?: points.last()
    val ratio = if (upper.y != lower.y) (y - lower.y) / (upper.y - lower.y) else 0f
    return lerp(lower.x, upper.x, ratio)
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private fun mapValue(
    inputValue: Float, inputStart: Float, inputEnd: Float, outputStart: Float, outputEnd: Float
): Float {
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