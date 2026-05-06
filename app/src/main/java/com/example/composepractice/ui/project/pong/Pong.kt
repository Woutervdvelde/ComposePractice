package com.example.composepractice.ui.project.pong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs

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
            .onSizeChanged { size ->
                pongState.onBoundsChanged(size)
            },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, offset ->
                        pongState.handlePlayerInput(offset.x, size.width.toFloat())
                    }
                },
        ) {
            pongState.draw(scope = this@Canvas)
        }
    }
}

class PongState(
    val playerWidth: Float = 350f,
    val playerHeight: Float = 50f,
    val ballSize: Float = 50f,
    val aiMaxSpeed: Float = 700f,
    val initialBallVelocity: Offset = Offset(500f, -800f),
    val maxHorizontalSpeed: Float = 1200f,
    val speedIncreaseAmount: Float = 100f,
    val onScored: (Scored) -> Unit = {},
) {
    private val playerState =
        PlayerState(width = playerWidth, height = playerHeight, maxSpeed = Float.POSITIVE_INFINITY)
    private val aiState = PlayerState(width = playerWidth, height = playerHeight, maxSpeed = aiMaxSpeed)
    private val ballState = BallState(size = ballSize, initialVelocity = initialBallVelocity)

    private var isPlaying by mutableStateOf(true)

    fun play() { isPlaying = true }
    fun pause() { isPlaying = false }

    internal fun onBoundsChanged(size: IntSize) {
        ballState.bounds = size
        playerState.position = playerState.position.copy(y = size.height - playerHeight - 50f)
        aiState.position = aiState.position.copy(y = 50f)
        resetBall()
    }

    internal fun handlePlayerInput(deltaX: Float, screenWidth: Float) {
        val newX = (playerState.position.x + deltaX).coerceIn(0f, screenWidth - playerState.width)
        playerState.position = playerState.position.copy(x = newX)
    }

    internal fun update(deltaTimeNanos: Long) {
        if (!isPlaying) return

        val dt = deltaTimeNanos / 1_000_000_000f
        val oldBallPos = ballState.position
        ballState.update(dt)
        val newBallPos = ballState.position

        val aiTarget = Offset(
            (ballState.getCenter().x - aiState.width / 2).coerceIn(
                0f,
                ballState.bounds.width.toFloat() - aiState.width
            ),
            aiState.position.y
        )
        aiState.moveTo(aiTarget, deltaTimeNanos)
        checkCollision(playerState, oldBallPos, newBallPos, isPlayer = true)
        checkCollision(aiState, oldBallPos, newBallPos, isPlayer = false)
        checkScored()
    }

    private fun checkCollision(
        paddle: PlayerState,
        oldPos: Offset,
        newPos: Offset,
        isPlayer: Boolean
    ) {
        val ballX = newPos.x + ballState.size / 2
        val paddleXRange = paddle.position.x..(paddle.position.x + paddle.width)

        val crossedLine = if (isPlayer) {
            oldPos.y + ballState.size <= paddle.position.y && newPos.y + ballState.size >= paddle.position.y
        } else {
            oldPos.y >= paddle.position.y + paddle.height && newPos.y <= paddle.position.y + paddle.height
        }

        if (crossedLine && ballX in paddleXRange) {
            val paddleCenter = paddle.position.x + (paddle.width / 2)
            val relativeIntersectX = (ballX - paddleCenter) / (paddle.width / 2)

            val kickVelocityX = relativeIntersectX * maxHorizontalSpeed
            val newVelocityX = (ballState.velocity.x * 0.3f + kickVelocityX).coerceIn(
                minimumValue = -maxHorizontalSpeed,
                maximumValue = maxHorizontalSpeed
            )

            val newVelocityY = if (isPlayer) {
                -(abs(ballState.velocity.y) + speedIncreaseAmount)
            } else {
                abs(ballState.velocity.y) + speedIncreaseAmount
            }

            ballState.velocity = Offset(newVelocityX, newVelocityY)

            val snappedY = if (isPlayer) paddle.position.y - ballState.size else paddle.position.y + paddle.height
            ballState.position = ballState.position.copy(y = snappedY)
        }
    }

    private fun checkScored() {
        if (ballState.position.y <= 0) {
            onScored(Scored.PLAYER)
            resetBall()
        }

        if (ballState.position.y >= ballState.bounds.height) {
            onScored(Scored.AI)
            resetBall()
        }
    }

    private fun resetBall() {
        ballState.position = Offset(
            x = ballState.bounds.width / 2f - ballState.size / 2,
            y = ballState.bounds.height / 2f
        )
        ballState.velocity = initialBallVelocity
    }

    internal fun draw(scope: DrawScope) {
        // Player
        scope.drawRoundRect(
            color = Color.Blue,
            topLeft = playerState.position.copy(x = playerState.position.x + 25f),
            size = Size(playerState.width - 25f, playerState.height),
            cornerRadius = CornerRadius(50f, 50f),
        )

        // AI
        scope.drawRoundRect(
            color = Color.Red,
            topLeft = aiState.position.copy(x = aiState.position.x + 25f),
            size = Size(aiState.width - 25f, aiState.height),
            cornerRadius = CornerRadius(50f, 50f),
        )

        // Ball
        scope.drawCircle(
            color = Color.White,
            center = ballState.getCenter(),
            radius = ballState.size / 2,
        )
    }

    companion object {
        enum class Scored {
            PLAYER, AI
        }
    }
}

private class PlayerState(val width: Float, val height: Float, val maxSpeed: Float) {
    var position by mutableStateOf(Offset.Zero)

    fun moveTo(target: Offset, deltaTime: Long) {
        val dt = deltaTime / 1_000_000_000f
        val diff = target - position
        if (diff.getDistance() > 5f) {
            val direction = diff / diff.getDistance()
            position += direction * (if (maxSpeed.isInfinite()) 1f else maxSpeed) * dt
        }
    }
}

private class BallState(val size: Float, val initialVelocity: Offset) {
    var bounds by mutableStateOf(IntSize.Zero)
    var position by mutableStateOf(Offset.Zero)
    var velocity by mutableStateOf(initialVelocity)

    fun update(dt: Float) {
        if (bounds == IntSize.Zero) return

        position += velocity * dt

        if (position.x <= 0 || position.x + size >= bounds.width) {
            velocity = velocity.copy(x = -velocity.x)
            position = position.copy(x = position.x.coerceIn(0f, bounds.width - size))
        }
    }

    fun getCenter() = position + Offset(size / 2f, size / 2f)
}