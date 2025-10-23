package com.example.composepractice.ui.project.counter

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composepractice.R

@Composable
fun Counter(
    day: Int,
    modifier: Modifier = Modifier,
) {
    val offset = 9 - day
    var currentNumber by remember { mutableIntStateOf(day + offset) }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false
            )
    ) {
        CountdownCard(day = currentNumber)
        CountdownCardRing(modifier.graphicsLayer { translationY = -8.dp.toPx() })
        CountdownCardAnimation(
            day = day,
            offset = offset,
            setCurrentNumber = { currentNumber = it }
        )
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

@Composable
private fun CountdownCardAnimation(
    day: Int,
    offset: Int,
    durationMs: Int = 750,
    setCurrentNumber: (Int) -> Unit
) {
    for (i in (day + offset - 1) downTo day) {
        val index = (day + offset - 1) - i
        var visible by remember { mutableStateOf(true) }
        val rotation = remember { Animatable(270f) }

        val t = index.toFloat() / (offset - 1)
        val scale = EaseInOut.transform(t)
        val delayMs = (scale * durationMs).toInt()

        if (visible) {
            CountdownCard(
                day = i,
                modifier = Modifier.graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    rotationX = rotation.value
                }
            )

            LaunchedEffect(Unit) {
                rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 1000, delayMillis = delayMs)
                )
                setCurrentNumber(i)
                visible = false
            }
        }
    }
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