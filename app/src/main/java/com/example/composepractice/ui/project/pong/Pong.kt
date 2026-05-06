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
import kotlin.div
import kotlin.plus

@Composable
fun PongScreen() {
    val pongState = remember { PongState() }
    Pong(
        pongState = pongState,
        modifier = Modifier
            .safeDrawingPadding(),
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
                pongState.playerState.position = pongState.playerState.position.copy(
                    y = size.height - pongState.playerState.height,
                )
                pongState.reset()
            },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, offset ->
                        val player = pongState.playerState
                        val newX = (player.position.x + offset.x).coerceIn(
                            minimumValue = 0f,
                            maximumValue = size.width - player.width
                        )
                        player.position = player.position.copy(x = newX)
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
                center = pongState.ballState.getCenter(),
                radius = pongState.ballState.size / 2,
            )
        }
    }
}

class PongState(
    playerSize: Offset = Offset(x = 300f, y = 200f),
    aiSize: Offset = Offset(x = 300f, y = 200f),
    aiMaxSpeed: Float = 400f,
    val speedIncrease: (Offset) -> Offset = { it.copy(it.x + 50f, it.y + 100f) },
) {
    val playerState = PlayerState(
        maxSpeed = Float.POSITIVE_INFINITY,
        width = playerSize.x,
        height = aiSize.y,
    )

    val aiState = PlayerState(
        maxSpeed = aiMaxSpeed,
        width = aiSize.x,
        height = aiSize.y,
    )
    val ballState = BallState(
//        onReset = { isPlaying = false }
    )

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
            when {
                ballState.position.y - (ballState.size / 2) > playerState.position.y -> score()
                ballState.position.y + (ballState.size / 2) < aiState.position.y + aiState.height -> score()
                else -> ballState.velocity = speedIncrease(ballState.velocity.copy(y = -ballState.velocity.y))
            }
        }
    }

    fun play() {
        isPlaying = true
    }

    fun pause() {
        isPlaying = false
    }
    
    private fun score() {
        reset()
    }
    
    fun reset() {
        // TODO save bounds/size in PongState
        ballState.position = Offset(ballState.bounds.width / 2f, ballState.bounds.height / 2f)
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
    val initialVelocity: Offset = Offset(500f, -400f),
    val onReset: () -> Unit = {},
) {
    var bounds by mutableStateOf(IntSize.Zero)
    var position by mutableStateOf(Offset.Zero)
    var velocity by mutableStateOf(initialVelocity)

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

    fun getCenter(): Offset =
        position + Offset(size / 2, size / 2)
}