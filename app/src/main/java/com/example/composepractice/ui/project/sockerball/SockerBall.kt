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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun SockerBallScreen() {
    val state = remember { SockerBallState() }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
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
                .background(Color.Green)
                .onGloballyPositioned { coordinates ->
                    containerSize = coordinates.size
                }
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
            state.shoot(containerSize, 50, power)
        }) {
            Text("Shoot Ball")
        }
    }
}

class SockerBallState {
    var position by mutableStateOf(Offset.Zero)
    var isVisible by mutableStateOf(false)
    var isAnimating by mutableStateOf(false)
    var velocity by mutableStateOf(Offset.Zero)

    fun shoot(initialBounds: IntSize, ballSize: Int, power: Float) {
        val maxSpeed = 80f
        val velocityMagnitude = maxSpeed * power.coerceIn(0f, 1f)

        position = Offset(
            x = (initialBounds.width / 2f) - (ballSize / 2f),
            y = (initialBounds.height / 2f) - (ballSize / 2f)
        )

        velocity = Offset(
            x = Random.nextDouble(-velocityMagnitude.toDouble(), velocityMagnitude.toDouble()).toFloat(),
            y = Random.nextDouble(-velocityMagnitude.toDouble(), velocityMagnitude.toDouble()).toFloat()
        )

        isVisible = true
        isAnimating = true
    }
}
@Composable
fun SockerBall(state: SockerBallState) {
    val ballSize = 50.dp
    val ballSizePx = with(LocalDensity.current) { ballSize.toPx() }
    val friction = .98f
    val dampening = .9f

    val alpha by animateFloatAsState(
        targetValue = if (state.isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "fade"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight.toFloat()

        LaunchedEffect(state.isAnimating) {
            if (!state.isAnimating) return@LaunchedEffect

            var currentVelocity = state.velocity
            var currentPos = state.position

            while (state.isAnimating) {
                currentVelocity *= friction

                currentPos += currentVelocity

                if (currentPos.x <= 0 || currentPos.x + ballSizePx >= maxWidth) {
                    currentVelocity = currentVelocity.copy(x = -currentVelocity.x * dampening)
                    currentPos = currentPos.copy(x = currentPos.x.coerceIn(0f, maxWidth - ballSizePx))
                }

                if (currentPos.y <= 0 || currentPos.y + ballSizePx >= maxHeight) {
                    currentVelocity = currentVelocity.copy(y = -currentVelocity.y * dampening)
                    currentPos = currentPos.copy(y = currentPos.y.coerceIn(0f, maxHeight - ballSizePx))
                }

                state.position = currentPos

                if (abs(currentVelocity.x) < 0.1f && abs(currentVelocity.y) < 0.1f) {
                    delay(2000)
                    state.isVisible = false
                    state.isAnimating = false
                }

                delay(10)
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
                    .background(Color.Red, CircleShape)
            )
        }
    }
}