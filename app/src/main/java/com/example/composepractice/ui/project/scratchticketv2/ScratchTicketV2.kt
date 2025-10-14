package com.example.composepractice.ui.project.scratchticketv2

import androidx.annotation.FloatRange
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.alpha
import androidx.core.graphics.get
import com.example.composepractice.R
import com.example.composepractice.ui.theme.FramnaGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal fun ScratchTicketV2() {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            val scratchState =
                rememberScratchState(scratchStrokeWidth = with(LocalDensity.current) { 25.dp.toPx() })
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

/**
 * Container for a scratch ticket, contains both the scratchable surface and the price content.
 * To allow the user to start dragging outside of the actual [ScratchTicketContainer] the [scratchState]
 * needs to have a reference to the [LayoutCoordinates] and [androidx.compose.ui.input.pointer.PointerInputScope.detectDragGestures].
 * for example:
 * ```
 * val scratchState = rememberScratchState()
 * Column(
 *     horizontalAlignment = Alignment.CenterHorizontally,
 *     modifier = Modifier
 *         .onGloballyPositioned { layoutCoordinates ->
 *             scratchState.externalLayoutCoordinates = layoutCoordinates
 *         }
 *         .pointerInput(Unit) {
 *             detectDragGestures(
 *                 onDrag = scratchState::handleExternalDrag,
 *                 onDragEnd = scratchState::handleExternalDragEnd
 *             )
 *         }
 * ) {
 *     ScratchTicketContainer(...)
 * }
 * ```
 * @param modifier Modifier to apply
 * @param scratchState state containing all relevant scratch data
 * @param scratchContent scratchable surface
 * @param content price content
 */
@Composable
fun ScratchTicketContainer(
    modifier: Modifier = Modifier,
    scratchState: ScratchState = rememberScratchState(),
    scratchContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val animatedScratchAlpha by animateFloatAsState(targetValue = scratchState.scratchOverlayAlpha.value)
    val hapticFeedback = LocalHapticFeedback.current

    scratchState.density = LocalDensity.current
    scratchState.haptics = hapticFeedback

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
                .onGloballyPositioned { layoutCoordinates ->
                    scratchState.localLayoutCoordinates = layoutCoordinates
                }
                .alpha(animatedScratchAlpha)
                .then(scratchState.drawModifier)
        ) {
            scratchContent()
        }
    }
}

@Composable
fun rememberScratchState(
    scratchStrokeWidth: Float = 50f,
    @FloatRange(from = 0.0, to = 1.0) threshold: Float = .5f,
    onThresholdReached: (progress: Float, scratchState: ScratchState) -> Unit = { _, _ -> }
): ScratchState {
    return remember {
        ScratchState(
            scratchStrokeWidth = scratchStrokeWidth,
            threshold = threshold,
            onThresholdReached = onThresholdReached
        )
    }
}

/**
 * Class keeping track of all ScratchTicket parameters and progress
 * @param scratchStrokeWidth Width of the scratching
 * @param threshold Threshold percentage between 0f and 1f that determines when to call [onThresholdReached]
 * @param onThresholdReached Callback method when set [threshold] is reached when user stops dragging,
 * contains the actual percentage that has been scratched and a reference to the [ScratchState]
 */
class ScratchState(
    private val scratchStrokeWidth: Float,
    @FloatRange(from = 0.0, to = 1.0) private val threshold: Float,
    private val onThresholdReached: (progress: Float, scratchState: ScratchState) -> Unit
) {
    internal var density: Density? = null
    internal var haptics: androidx.compose.ui.hapticfeedback.HapticFeedback? = null
    internal var scratchOverlayAlpha: MutableState<Float> = mutableFloatStateOf(1f)
    internal var externalLayoutCoordinates: LayoutCoordinates? = null
    internal var localLayoutCoordinates: LayoutCoordinates? = null

    private var finishedScratching: Boolean = false
    private val paths: MutableList<Path> = mutableListOf()
    private val currentPoints: MutableList<Offset> = mutableListOf()
    private val _invalidateTrigger = mutableIntStateOf(0)

    private var progressBitmap: ImageBitmap? = null
    private var progressCanvas: Canvas? = null

    fun handleExternalDrag(change: PointerInputChange, dragAmount: Offset) {
        if (finishedScratching) return
        if (externalLayoutCoordinates == null || localLayoutCoordinates == null) return

        val localPos = localLayoutCoordinates!!.localPositionOf(
            externalLayoutCoordinates!!, change.position
        )
        if (localPos.higherThan(Offset.Zero) && localPos.lowerThan(localLayoutCoordinates!!.size.toOffset())) {
            handleOnDrag(
                change = change,
                position = localPos,
                dragAmount = dragAmount
            )
        }
    }

    fun handleExternalDragEnd() {
        onDragEnd()
    }

    fun reset() {
        paths.clear()
        currentPoints.clear()
        progressBitmap = null
        progressCanvas = null
        finishedScratching = false
        _invalidateTrigger.intValue++
    }

    fun reveal(showAnimation: Boolean = false) {
        scratchOverlayAlpha.value = 0f
    }

    internal val onDrag: (PointerInputChange, Offset) -> Unit = { change, dragAmount ->
        if (!finishedScratching) {
            handleOnDrag(
                change = change,
                position = change.position,
                dragAmount = dragAmount
            )
        }
    }


    internal val onDragEnd: () -> Unit = {
        if (!finishedScratching) {
            if (currentPoints.size > 1) {
                val smoothPath = buildSmoothPath(currentPoints)
                paths.add(smoothPath)
                updateProgressBitmap(smoothPath)
            }
            currentPoints.clear()
            _invalidateTrigger.intValue++
        }
    }

    internal val drawModifier: Modifier
        get() = Modifier.drawWithCache {
            // Only reading state to invalidate drawing and retrigger a draw cycle
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

    private fun handleOnDrag(change: PointerInputChange, position: Offset, dragAmount: Offset) {
        change.consume()
        addPoint(position + dragAmount)
        _invalidateTrigger.intValue++
        haptics?.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    private fun addPoint(point: Offset) {
        if (currentPoints.isEmpty() || (currentPoints.last() - point).getDistance() > 2f) {
            currentPoints.add(point)
        }
    }

    /** Checks if progressBitmap is already created, otherwise creates it using the current LayoutCoordinates */
    private fun checkOrCreateBitmap() {
        if (progressBitmap == null && localLayoutCoordinates != null) {
            val bitmap = ImageBitmap(
                localLayoutCoordinates!!.size.width,
                localLayoutCoordinates!!.size.height,
            )
            val canvas = Canvas(bitmap)

            progressBitmap = bitmap
            progressCanvas = canvas
        }
    }

    private fun updateProgressBitmap(newPath: Path) = CoroutineScope(Dispatchers.IO).launch {
        checkOrCreateBitmap()

        progressBitmap?.let { bitmap ->
            progressCanvas?.let { canvas ->
                CanvasDrawScope().draw(
                    density = density ?: Density(1f),
                    layoutDirection = LayoutDirection.Ltr,
                    size = Size(bitmap.width.toFloat(), bitmap.height.toFloat()),
                    canvas = canvas
                ) {
                    drawPath(
                        path = newPath,
                        color = Color.Black,
                        style = Stroke(
                            width = scratchStrokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                val androidBitmap = bitmap.asAndroidBitmap()

                var coloredPixels = 0
                val totalPixels = bitmap.width * bitmap.height

                for (x in 0..bitmap.width - 1) {
                    for (y in 0..bitmap.height - 1) {
                        val alpha = androidBitmap[x, y].alpha
                        if (alpha > 0) {
                            coloredPixels++
                        }
                    }
                }

                val percentage = (coloredPixels.toFloat() / totalPixels.toFloat())
                if (percentage > threshold) {
                    onThresholdReached(percentage, this@ScratchState)
                    finishedScratching = true
                }
            }
        }
    }

    private fun buildSmoothPath(points: List<Offset>): Path {
        val path = Path()
        if (points.isEmpty() || points.size == 1) return path

        path.moveTo(points.first().x, points.first().y)

        for (i in 1 until points.size - 1) {
            val midPoint = (points[i] + points[i + 1]) / 2f
            path.quadraticTo(points[i].x, points[i].y, midPoint.x, midPoint.y)
        }
        path.lineTo(points.last().x, points.last().y)

        return path
    }

    private fun Offset.lowerThan(other: Offset): Boolean = (this.x < other.x && this.y < other.y)
    private fun Offset.higherThan(other: Offset): Boolean = (this.x > other.x && this.y > other.y)
    private fun IntSize.toOffset(): Offset = Offset(this.width.toFloat(), this.height.toFloat())
}