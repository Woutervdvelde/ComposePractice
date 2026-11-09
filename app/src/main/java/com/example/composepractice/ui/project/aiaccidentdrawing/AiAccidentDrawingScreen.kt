package com.example.composepractice.ui.project.aiaccidentdrawing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.DrawingScreen
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.LoadingScreen
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.ModelScreen
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.ResultErrorScreen
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.ResultScreen
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.SketchState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

private sealed interface Screen {
    data object ModelSelect : Screen
    data object Draw : Screen
    data object Loading : Screen
    data class Result(val sketch: ImageBitmap, val analysis: AccidentAnalysis, val stats: GenerationStats) : Screen
    data class Error(val sketch: ImageBitmap, val message: String) : Screen
}

@Composable
internal fun AiAccidentDrawingScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf<Screen>(Screen.ModelSelect) }
    val engine = remember { AiAccidentEngine(context) }
    val sketchState = remember { SketchState() }

    fun analyze(sketch: ImageBitmap, request: suspend () -> AnalysisResponse?) {
        screen = Screen.Loading
        scope.launch {
            screen = runCatching { request() }.fold(
                onSuccess = { response ->
                    response?.let { Screen.Result(sketch, it.analysis, it.stats) }
                        ?: Screen.Error(sketch, "The AI response could not be parsed")
                },
                onFailure = { Screen.Error(sketch, it.message ?: "Something went wrong") },
            )
        }
    }

    Scaffold(
        containerColor = Color.White,
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            AnimatedContent(
                targetState = screen,
                contentKey = { it::class },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.fillMaxSize(),
            ) { state ->
                when (state) {
                    Screen.ModelSelect -> ModelScreen(
                        engine = engine,
                        onModelLoaded = { screen = Screen.Draw },
                    )
                    Screen.Draw -> DrawingScreen(
                        sketchState = sketchState,
                        onSubmit = { sketch ->
                            analyze(sketch) { engine.sendImage(sketch.asAndroidBitmap()).firstOrNull() }
                        },
                    )
                    Screen.Loading -> LoadingScreen()
                    is Screen.Result -> ResultScreen(
                        sketch = state.sketch,
                        analysis = state.analysis,
                        stats = state.stats,
                        onAnswer = { option ->
                            // TODO
                        },
                        onCustomAnswer = { answer ->
                            // TODO
                        },
                        onRedraw = { screen = Screen.Draw },
                    )
                    is Screen.Error -> ResultErrorScreen(
                        sketch = state.sketch,
                        message = state.message,
                        onRedraw = { screen = Screen.Draw },
                    )
                }
            }
        }
    }
}