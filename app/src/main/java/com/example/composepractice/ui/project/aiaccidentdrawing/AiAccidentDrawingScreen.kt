package com.example.composepractice.ui.project.aiaccidentdrawing

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.DrawingScreen
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.ModelScreen

private enum class Screen {
    MODEL_SELECT, DRAW
}

@Composable
internal fun AiAccidentDrawingScreen() {
    val context = LocalContext.current
    var screen by rememberSaveable { mutableStateOf(Screen.MODEL_SELECT) }
    val engine = remember { AiAccidentEngine(context) }

    Scaffold(
        containerColor = Color.White,
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            Crossfade(
                targetState = screen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
            ) { state ->
                when (state) {
                    Screen.MODEL_SELECT -> ModelScreen(
                        engine = engine,
                        onModelLoaded = { screen = Screen.DRAW },
                    )
                    Screen.DRAW -> DrawingScreen(engine)
                }
            }
        }
    }
}