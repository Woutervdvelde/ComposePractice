package com.example.composepractice.ui.project.counter

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.composepractice.R

/**
 * Counter display for two digits. Starts animating when composed.
 * @param start Number the animation should start at
 * @param end The final target number the counter will land on
 * @param flipDelay Delay between the start of each flip
 * @param flipDuration The duration of a single flip
 * @param flipEasing Easing function for the flip animation
 */
@Composable
fun Counter(
    start: Int,
    end: Int,
    modifier: Modifier = Modifier,
    flipDelay: Float = 250f,
    flipDuration: Float = 1000f,
    flipEasing: Easing = EaseInOut
) {
    val tens = mutableListOf<Int>()
    val ones = mutableListOf<Int>()
    for (n in start downTo end) {
        tens += n / 10
        ones += n % 10
    }

    Row(modifier = modifier) {
        SingleCounter(
            animationList = tens,
            flipDelay = flipDelay,
            flipDuration = flipDuration,
            flipEasing = flipEasing
        )
        SingleCounter(
            animationList = ones,
            flipDelay = flipDelay,
            flipDuration = flipDuration,
            flipEasing = flipEasing
        )
    }
}

@Composable
private fun SingleCounter(
    animationList: List<Int>,
    flipDelay: Float,
    flipDuration: Float,
    flipEasing: Easing,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.TopCenter, modifier = modifier) {
        CountdownCard(day = animationList.first())
        CountdownCardRing(modifier.graphicsLayer { translationY = -8.dp.toPx() })
        animationList.forEachIndexed { index, number ->
            val previous = animationList.getOrNull(index - 1)
            if (number == previous || index == 0) return@forEachIndexed

            AnimatedCountdownCard(
                number = number,
                index = index,
                flipDelay = flipDelay,
                flipDuration = flipDuration,
                flipEasing = flipEasing,
            )
        }
    }
}

@Composable
fun AnimatedCountdownCard(
    number: Int,
    index: Int,
    flipDelay: Float,
    flipDuration: Float,
    flipEasing: Easing,
) {
    val rotationX = remember { Animatable(270f) }
    val zIndex by remember { derivedStateOf { if (rotationX.value > 90f) -index.toFloat() else 0f } }

    LaunchedEffect(number) {
        val startDelay = index * flipDelay

        rotationX.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = flipDuration.toInt(),
                delayMillis = startDelay.toInt(),
                easing = flipEasing
            )
        )
    }

    CountdownCard(
        day = number,
        modifier = Modifier
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 0f)
                this.rotationX = rotationX.value
            }
            .zIndex(zIndex)
    )
}

@Composable
private fun CountdownCard(day: Int, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(R.drawable.advent_calendar_countdown_cutout),
            contentDescription = null,
            modifier = Modifier.width(64.dp)
        )

        Text(
            modifier = Modifier.offset { IntOffset(x = 0, y = 10.dp.roundToPx()) },
            text = day.toString(),
            fontSize = 48.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun CountdownCardRing(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.advent_calendar_countdown_ring),
        contentDescription = null,
        modifier = modifier.width(4.dp)
    )
}

@Preview
@Composable
private fun CounterPreview() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Counter(
            start = 21,
            end = 7,
        )
    }
}