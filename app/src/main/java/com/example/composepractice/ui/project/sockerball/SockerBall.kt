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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

@Composable
fun SockerBallScreen() {
    val state = remember { SockerBallState() }
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
            state.shoot(50, power)
        }) {
            Text("Shoot Ball")
        }
    }
}

class SockerBallState {
    private val maxSpeed = 10000f
    private val friction = .2f
    private val dampening = .9f
    
    // Set by SockerBall composable
    var bounds by mutableStateOf(IntSize.Zero)
    var ballSizePx by mutableFloatStateOf(0f)
    
    var position by mutableStateOf(Offset.Zero)
    var isVisible by mutableStateOf(false)
    var isAnimating by mutableStateOf(false)
    var velocity by mutableStateOf(Offset.Zero)

    fun shoot(ballSize: Int, power: Float) {
        val velocityMagnitude = maxSpeed * power.coerceIn(0f, 1f)

        position = Offset(
            x = (bounds.width / 2f) - (ballSize / 2f),
            y = (bounds.height / 2f) - (ballSize / 2f)
        )

        velocity = Offset(
            x = Random.nextDouble(-velocityMagnitude.toDouble(), velocityMagnitude.toDouble()).toFloat(),
            y = Random.nextDouble(-velocityMagnitude.toDouble(), velocityMagnitude.toDouble()).toFloat()
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
    val ballSize = 50.dp
    val ballSizePx = with(LocalDensity.current) { ballSize.toPx() }

    val alpha by animateFloatAsState(
        targetValue = if (state.isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "fade"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        state.bounds = IntSize(constraints.maxWidth, constraints.maxHeight)
        state.ballSizePx = ballSizePx

        LaunchedEffect(state.isAnimating) {
            if (!state.isAnimating) return@LaunchedEffect

            var lastFrame = 0L
            while (state.isAnimating) {
                val currentNano = awaitFrame()
                if (lastFrame != 0L) {
                    val frameTime = currentNano - lastFrame
                    
                    state.next(frameTime)
                    if (abs(state.velocity.x) < 0.1f && abs(state.velocity.y) < 0.1f) {
                        delay(2000)
                        state.isVisible = false
                        state.isAnimating = false
                    }
                }
                lastFrame = currentNano
            }
        }

        if (alpha > 0f) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.translationX = state.position.x
                        this.translationY = state.position.y
                        this.alpha = alpha
                    }
                    .size(ballSize)
                    .background(Color.White, CircleShape)
            )
        }
    }
}