package com.example.composepractice.ui.project.lock

import android.annotation.SuppressLint
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.example.composepractice.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LockScreen() {
    val view = LocalView.current
    val vibrator = view.context.getSystemService(Vibrator::class.java)

    Scaffold(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) { _ ->
        var targetTick by remember { mutableIntStateOf((0..59).random()) }

        fun setRandom() {
            targetTick = (0..59).random()
        }

        fun correctTick() {
//            setRandom()
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    50,
                    255
                )
            )
        }

        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Indicator(false)
                Indicator(false)
                Indicator(false)
            }

            Lock(
                targetTick = targetTick,
                onTick = { tick ->
                    if (tick == targetTick) {
                        correctTick()
                    } else {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                50,
                                50
                            )
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun Indicator(correct: Boolean) {
    val color = if (correct) Color.Green else Color.Red
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color, CircleShape)
    )
}

@Composable
private fun Lock(
    targetTick: Int,
    onTick: (Int) -> Unit = {},
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var lastTick by remember { mutableIntStateOf(0) }
    var targetDirection by remember { mutableIntStateOf(0) }

    val state = rememberTransformableState { _, _, rotationChange ->
        rotation += rotationChange
        val tick = (60 - ((rotation % 360).toInt() / 6)) % 60
        val direction = if (tick < lastTick) -1 else 1

        if (tick != lastTick) {
            onTick(tick)
            lastTick = tick
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LockCenterIndicator()
        Box(
            Modifier
                .graphicsLayer(
                    rotationZ = rotation,
                )
                .transformable(state = state)
                .paint(
                    painter = painterResource(R.drawable.lock),
                    contentScale = ContentScale.FillBounds
                )
                .aspectRatio(1f)
                .fillMaxSize()
        )
    }
}

@Composable
private fun LockCenterIndicator() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .drawWithCache {
                val shape = RoundedPolygon(
                    numVertices = 3,
                    radius = size.minDimension / 2,
                    centerX = size.width / 2,
                    centerY = size.height / 2,
                    rounding = CornerRounding(size.minDimension / 10f, smoothing = 0.1f)
                )
                val roundedPath = shape.toPath().asComposePath()
                onDrawBehind {
                    rotate(90f) {
                        drawPath(roundedPath, color = Color.Yellow)
                    }
                }
            }
    )
}