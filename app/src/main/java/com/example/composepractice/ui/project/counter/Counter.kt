package com.example.composepractice.ui.project.counter

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composepractice.R

@Composable
fun Counter(
    day: Int,
    modifier: Modifier = Modifier,
    start: Int = 21,
    end: Int = 8,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium.copy(fontSize = 48.sp),
) {
    val tens = mutableListOf<Int>()
    val ones = mutableListOf<Int>()
    for (n in start downTo end) {
        tens += n / 10
        ones += n % 10
    }

    Row(modifier = modifier) {
        SingleCounter(
            animationList = tens
        )
        SingleCounter(
            animationList = ones
        )
    }
}

@Composable
private fun SingleCounter(
    animationList: List<Int>,
    modifier: Modifier = Modifier
) {
    val animationDuration = 5000
    val flipDelay = 1000f
    val flipDuration = 500f
    val animation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animation.animateTo(
            targetValue = animationList.size * flipDelay + (flipDuration - flipDelay),
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
    ) {
        CountdownCard(day = animationList.first())
        CountdownCardRing(modifier.graphicsLayer { translationY = -8.dp.toPx() })

        animationList.forEachIndexed { index, number ->
            val startFrom = index * flipDelay
            val endAt = startFrom + flipDuration

            val timeElapsed = (animation.value - startFrom)
                .coerceAtLeast(0f)
                .coerceAtMost(flipDuration)

            val progress = timeElapsed / flipDuration
            val rotationX = (1f - progress) * 270f

            CountdownCard(
                day = number,
                modifier = Modifier.graphicsLayer {
                    this.transformOrigin = TransformOrigin(0.5f, 0f)
                    this.rotationX = rotationX
                }
            )
        }
    }
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
            modifier = Modifier.offset(y = 10.dp),
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
            day = 1
        )
    }
}