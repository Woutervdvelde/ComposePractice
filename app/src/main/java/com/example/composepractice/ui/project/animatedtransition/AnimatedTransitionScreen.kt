package com.example.composepractice.ui.project.animatedtransition

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun AnimatedTransitionScreen() {
    var visible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                CustomAnimatedVisibility(
                    visible = visible,
                ) {
                    CustomBox(text = "Custom✨")
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 4 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 }),
                    modifier = Modifier.graphicsLayer {
                        this.compositingStrategy = CompositingStrategy.Auto
                        this.outsets = LayerOutsets(22.dp)
                    },
                ) {
                    CustomBox(text = "Default🤢")
                }
            }

            Button(
                onClick = { visible = !visible },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) { Text("Toggle", modifier = Modifier.padding(vertical = 16.dp, horizontal = 32.dp)) }
        }
    }
}

private fun Modifier.customShadow() = dropShadow(
    shape = RoundedCornerShape(16.dp),
    shadow = Shadow(
        radius = 16.dp,
        color = Color.Blue,
        offset = DpOffset(x = 0.dp, y = 6.dp),
    ),
)

@Composable
private fun CustomBox(text: String) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .background(Color.Blue)
            .customShadow(),
    ) {
        Text(text = text, color = Color.White)
    }
}

@Composable
private fun CustomAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val animatedY by animateFloatAsState(
        targetValue = if (visible) 0f else -.25f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    )
    

    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = animatedAlpha
                this.compositingStrategy = CompositingStrategy.ModulateAlpha
            }
            .drawWithContent {
                withTransform(
                    {
                        translate(top = this.size.height * animatedY)
                    },
                ) {
                    this@drawWithContent.drawContent()
                }
            },
    ) {
        content()
    }
}
