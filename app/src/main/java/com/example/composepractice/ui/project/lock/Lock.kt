package com.example.composepractice.ui.project.lock

import android.annotation.SuppressLint
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import com.example.composepractice.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LockScreen() {
    Scaffold(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) { _ ->
        Lock()
    }
}

@Composable
fun Lock() {
    val view = LocalView.current
    val vibrator = view.context.getSystemService(Vibrator::class.java)
    val random by remember { mutableIntStateOf((1..60).random()) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var lastTick by remember {
        mutableIntStateOf(0)
    }

    val state = rememberTransformableState { _, _, rotationChange ->
        rotation += rotationChange
        val tick = (rotation % 360).toInt() / 6
        if (tick != lastTick) {
            lastTick = tick
            vibrator.vibrate(VibrationEffect.createOneShot(50, if (tick == random) 255 else 75))
        }
    }
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