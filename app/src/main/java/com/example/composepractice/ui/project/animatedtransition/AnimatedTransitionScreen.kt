package com.example.composepractice.ui.project.animatedtransition

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
internal fun AnimatedTransitionScreen() {
    var visible by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    
    Scaffold { scaffoldPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
        ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 4 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 }),
                    modifier = Modifier.graphicsLayer {
//                        outsets = LayerOutsets(bottom = 100.dp)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.Blue)
                            .drawBehind {
                                with(density) {
                                    drawRect(
                                        color = Color.Red,
                                        size = Size(150.dp.toPx(), 200.dp.toPx()),
                                        topLeft = Offset(0f, 50.dp.toPx())
                                    )
                                }
                            }
                    )
            }

            Button(
                onClick = { visible = !visible },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) { Text("Toggle") }
        }
    }
}