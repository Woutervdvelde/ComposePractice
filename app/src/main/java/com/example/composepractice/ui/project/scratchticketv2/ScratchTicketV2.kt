package com.example.composepractice.ui.project.scratchticketv2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composepractice.R
import com.example.composepractice.ui.theme.FramnaGreen

@Composable
internal fun ScratchTicketV2() {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            val scratchState =
                rememberScratchState(scratchStrokeWidth = with(LocalDensity.current) { 20.dp.toPx() })
            ScratchTicketContainer(
                modifier = Modifier
                    .width(400.dp)
                    .height(250.dp),
                scratchState = scratchState,
                scratchContent = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(FramnaGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.framna),
                            contentDescription = null
                        )
                    }
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Yellow)
                ) {
                    Text("\uD83C\uDFC6", fontSize = 100.sp) // Trophy emoji
                }
            }

            Button(
                onClick = scratchState::reset
            ) {
                Text("Reset ticket")
            }
        }
    }
}

@Composable
fun ScratchTicketContainer(
    modifier: Modifier = Modifier,
    scratchState: ScratchState = rememberScratchState(),
    scratchContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectDragGestures(
                        onDrag = scratchState.onDrag,
                        onDragEnd = scratchState.onDragEnd
                    )
                }
                .then(scratchState.drawModifier)
        ) {
            scratchContent()
        }
    }
}

@Composable
fun rememberScratchState(
    scratchStrokeWidth: Float = 50f
): ScratchState {
    return remember {
        ScratchState(scratchStrokeWidth)
    }
}

class ScratchState(
    private val scratchStrokeWidth: Float
) {
    private val paths: MutableList<Path> = mutableListOf()
    private val currentPoints: MutableList<Offset> = mutableListOf()
    private val _invalidateTrigger = mutableIntStateOf(0)

    fun reset() {
        paths.clear()
        currentPoints.clear()
        _invalidateTrigger.intValue++
    }

    internal val onDrag: (PointerInputChange, Offset) -> Unit = { change, dragAmount ->
        change.consume()
        addPoint(change.position + dragAmount)
        _invalidateTrigger.intValue++
    }

    internal val onDragEnd: () -> Unit = {
        if (currentPoints.size > 1) {
            paths.add(buildSmoothPath(currentPoints))
        }
        currentPoints.clear()
        _invalidateTrigger.intValue++
    }

    internal val drawModifier: Modifier
        get() = Modifier.drawWithCache {
            val trigger = _invalidateTrigger.intValue

            val allPaths = mutableListOf<Path>()
            allPaths.addAll(paths)
            if (currentPoints.size > 1) {
                allPaths.add(buildSmoothPath(currentPoints))
            }

            val paint = Paint().apply {
                isAntiAlias = true
                style = PaintingStyle.Stroke
                strokeWidth = scratchStrokeWidth
                strokeCap = StrokeCap.Round
                strokeJoin = StrokeJoin.Round
            }

            onDrawWithContent {
                drawIntoCanvas { canvas ->
                    canvas.saveLayer(size.toRect(), Paint())
                    drawContent()
                    paint.blendMode = BlendMode.Clear
                    allPaths.forEach { path ->
                        canvas.drawPath(path, paint)
                    }
                    canvas.restore()
                }
            }
        }

    private fun addPoint(point: Offset) {
        if (currentPoints.isEmpty() || (currentPoints.last() - point).getDistance() > 2f) {
            currentPoints.add(point)
        }
    }

    private fun buildSmoothPath(points: List<Offset>): Path {
        val path = Path()
        if (points.isEmpty()) return path

        path.moveTo(points.first().x, points.first().y)

        if (points.size == 1) return path

        for (i in 1 until points.size - 1) {
            val midPoint = (points[i] + points[i + 1]) / 2f
            path.quadraticTo(points[i].x, points[i].y, midPoint.x, midPoint.y)
        }
        path.lineTo(points.last().x, points.last().y)

        return path
    }
}