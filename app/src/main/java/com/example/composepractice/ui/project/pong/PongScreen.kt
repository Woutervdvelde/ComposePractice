package com.example.composepractice.ui.project.pong

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.composepractice.ui.util.LockScreenOrientation
import com.example.composepractice.ui.util.Orientation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PongScreen() {
    var delayStart: suspend () -> Unit = {}
    val coroutineScope = rememberCoroutineScope()
    val pongState = remember {
        PongState(
            onScored = { scored ->
                coroutineScope.launch {
                    delayStart()
                }
            }
        )
    }

    delayStart = {
        pongState.pause()
        delay(2000L)
        pongState.play()
    }

    Pong(
        pongState = pongState,
        modifier = Modifier.safeDrawingPadding(),
    )

    LockScreenOrientation(orientation = Orientation.PORTRAIT)
}
