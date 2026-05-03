package com.example.composepractice.ui.project.sockerball

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SockerBallScreen() {
    val size = with(LocalDensity.current) { 50.dp.toPx() }
    val state = remember { SockerBallState(size) }
    var power by remember { mutableFloatStateOf(0.5f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(.75f)
                .fillMaxHeight(.5f)
                .background(Color(0xFF0A6419))
        ) {
            SockerBall(state)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Power: ${(power * 100).toInt()}%")
        Slider(
            value = power,
            onValueChange = { power = it },
            modifier = Modifier.fillMaxWidth(0.5f)
        )

        Button(onClick = {
            state.shoot(power)
        }) {
            Text("Shoot Ball")
        }
    }
}

class SockerBallState(
    val ballSizePx: Float
) {
    private val friction = .2f
    private val dampening = .9f
    
    // Set by SockerBall composable
    var bounds by mutableStateOf(IntSize.Zero)

    var position by mutableStateOf(Offset.Zero)
    var isVisible by mutableStateOf(false)
    var isAnimating by mutableStateOf(false)
    var velocity by mutableStateOf(Offset.Zero)

    fun shoot(power: Float) {
        val baseSpeed = 4000f
        val randomAngle = Random.nextDouble(0.0, 2 * Math.PI)

        position = Offset(
            x = (bounds.width / 2f) - (ballSizePx / 2f),
            y = (bounds.height / 2f) - (ballSizePx / 2f)
        )

        velocity = Offset(
            x = (cos(randomAngle) * power * baseSpeed).toFloat(),
            y = (sin(randomAngle) * power * baseSpeed).toFloat()
        )

        isVisible = true
        isAnimating = true
    }
    
    fun next(deltaTime: Long) {
        val dt = deltaTime / 1_000_000_000f
        val frameFriction = friction.toDouble().pow(dt.toDouble()).toFloat()

        velocity *= frameFriction
        position += velocity * dt
        
        if (position.x <= 0 || position.x + ballSizePx >= bounds.width) {
            velocity = velocity.copy(x = -velocity.x * dampening)
            position = position.copy(x = position.x.coerceIn(0f, bounds.width - ballSizePx))
        }

        if (position.y <= 0 || position.y + ballSizePx >= bounds.height) {
            velocity = velocity.copy(y = -velocity.y * dampening)
            position = position.copy(y = position.y.coerceIn(0f, bounds.height - ballSizePx))
        }
    }
}
@Composable
fun SockerBall(state: SockerBallState) {
    val density = LocalDensity.current
    val ballSizeDp = remember(density) { with(density) { state.ballSizePx.toDp() } }

    val alpha by animateFloatAsState(
        targetValue = if (state.isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "fade"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        state.bounds = IntSize(constraints.maxWidth, constraints.maxHeight)

        LaunchedEffect(state.isAnimating) {
            if (!state.isAnimating) return@LaunchedEffect

            var lastFrame = System.nanoTime()
            while (state.isAnimating) {
                val currentNano = awaitFrame()
                val frameTime = currentNano - lastFrame

                state.next(frameTime)

                if (state.velocity.getDistance() < 10f) {
                    delay(2000)
                    state.isVisible = false
                    state.isAnimating = false
                }
                lastFrame = currentNano
            }
        }

        if (alpha > 0f) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(state.position.x.roundToInt(), state.position.y.roundToInt())
                    }
                    .graphicsLayer { this.alpha = alpha }
                    .size(ballSizeDp)
                    .background(Color.White, CircleShape)
            )
        }
    }
}