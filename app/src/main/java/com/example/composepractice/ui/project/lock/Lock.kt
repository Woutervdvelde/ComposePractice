package com.example.composepractice.ui.project.lock

import android.annotation.SuppressLint
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradient
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LockScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { _ ->
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
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(25), intArrayOf(if (tick == random) 255 else 55), -1))
                Log.d("TAG", "LockScreen: $tick | $random")
            }
        }
        Box(
            Modifier
                .graphicsLayer(
                    rotationZ = rotation,
                )
                .transformable(state = state)
                .background(Brush.horizontalGradient(listOf(Color.Red, Color.Blue)), CircleShape)
                .aspectRatio(1f)
                .fillMaxSize()
        )
    }
}