package com.example.composepractice.ui.project.pong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.composepractice.ui.util.LockScreenOrientation
import com.example.composepractice.ui.util.Orientation

@Composable
fun PongScreen() {
    val pongState = remember { PongState() }
    Pong(
        pongState = pongState,
        modifier = Modifier
            .safeDrawingPadding()
    )

    LockScreenOrientation(orientation = Orientation.PORTRAIT)
}


@Composable
fun Pong(
    pongState: PongState,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        var previousTimeNanos = withFrameNanos { it }
        while (true) {
            withFrameNanos { currentTimeNanos ->
                val deltaTimeNanos = currentTimeNanos - previousTimeNanos
                previousTimeNanos = currentTimeNanos

                pongState.update(deltaTimeNanos)
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .onSizeChanged { size ->
                pongState.ballState.bounds = size
                if (pongState.playerState.position == Offset.Zero) {
                    pongState.playerState.position = Offset(size.width / 2f, size.height - 100f)
                    pongState.aiState.position = Offset(size.width / 2f, 100f)
                    pongState.ballState.position = Offset(size.width / 2f, size.height / 2f)
                    pongState.ballState.velocity = Offset(400f, 400f)
                }
            },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val newX = (change.position.x - pongState.playerState.width / 2)
                            .coerceIn(0f, size.width.toFloat() - pongState.playerState.width)
                        pongState.playerState.position = pongState.playerState.position.copy(x = newX)
                    }
                },
        ) {
            drawRoundRect(
                color = Color.Blue,
                topLeft = pongState.playerState.position,
                size = Size(pongState.playerState.width, pongState.playerState.height),
                cornerRadius = CornerRadius(50f, 50f),
            )

            drawRoundRect(
                color = Color.Red,
                topLeft = pongState.aiState.position,
                size = Size(pongState.aiState.width, pongState.aiState.height),
                cornerRadius = CornerRadius(50f, 50f),
            )

            drawCircle(
                color = Color.White,
                center = pongState.ballState.position + Offset(pongState.ballState.size / 2, pongState.ballState.size / 2),
                radius = pongState.ballState.size / 2,
            )
        }
    }
}

class PongState {
    val playerState = PlayerState(maxSpeed = Float.POSITIVE_INFINITY, width = 500f)
    val aiState = PlayerState(maxSpeed = 400f)
    val ballState = BallState(onReset = { isPlaying = false })

    private var isPlaying: Boolean = true

    fun update(deltaTimeNanos: Long) {
        if (!isPlaying) return
        
        ballState.next(deltaTimeNanos)

        val aiTarget = Offset(
            (ballState.position.x - aiState.width / 2).coerceIn(0f, ballState.bounds.width.toFloat() - aiState.width),
            aiState.position.y,
        )
        aiState.moveTo(aiTarget, deltaTimeNanos)

        val ballRect = Rect(ballState.position, Size(ballState.size, ballState.size))
        val playerRect = Rect(playerState.position, Size(playerState.width, playerState.height))
        val aiRect = Rect(aiState.position, Size(aiState.width, aiState.height))

        if (ballRect.overlaps(playerRect) || ballRect.overlaps(aiRect)) {
            ballState.velocity = ballState.velocity.copy(y = -ballState.velocity.y * 1.2f)
        }
    }

    fun play() {
        isPlaying = true
    }

    fun pause() {
        isPlaying = false
    }
}

class PlayerState(
    val maxSpeed: Float,
    val width: Float = 300f,
    val height: Float = 100f,
) {
    var position by mutableStateOf(Offset.Zero)

    fun moveTo(target: Offset, deltaTime: Long) {
        val dt = deltaTime / 1_000_000_000f
        val diff = target - position
        if (diff.getDistance() > 5f) {
            val direction = diff / diff.getDistance()
            position += direction * maxSpeed * dt
        }
    }
}

/**
 * Ball state
 * @param size Size of the ball in px.
 * @param initialVelocity The velocity the ball starts moving with.
 * @param onReset Callback when the ball goes out of bounds vertically.
 */
class BallState(
    val size: Float = 50f,
    val initialVelocity: Offset = Offset(500f, 400f),
    val onReset: () -> Unit = {}
) {
    var bounds by mutableStateOf(IntSize.Zero)
    var position by mutableStateOf(Offset.Zero)
    var velocity by mutableStateOf(Offset.Zero)

    fun next(deltaTime: Long) {
        if (bounds == IntSize.Zero) return

        val dt = deltaTime / 1_000_000_000f
        position += velocity * dt

        if (position.x <= 0 || position.x + size >= bounds.width) {
            velocity = velocity.copy(x = -velocity.x)
            position = position.copy(x = position.x.coerceIn(0f, bounds.width - size))
        }

        if (position.y <= 0 || position.y + size >= bounds.height) {
            position = Offset(bounds.width / 2f, bounds.height / 2f)
            velocity = Offset(initialVelocity.x, if (velocity.y > 0) -initialVelocity.y else initialVelocity.y)
            onReset()
        }
    }
}