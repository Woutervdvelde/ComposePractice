package com.example.composepractice.ui.project.gradient

import android.graphics.Shader
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.composepractice.R


@Composable
internal fun GradientAnimationScreen() {
    var showLottie by remember { mutableStateOf(false) }

    Box {
        Crossfade(
            targetState = showLottie
        ) {
            if (it) {
                LottieReference()
            } else {
                val infiniteTransition = rememberInfiniteTransition()
                val gradientAnimationManager = rememberGradientAnimationManager(
                    gradientCircles = GradientCircles.asReversed(),
                    infiniteTransition = infiniteTransition
                )

                GradientAnimation(
                    gradientAnimationManager = gradientAnimationManager,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        FloatingActionButton(
            onClick = { showLottie = !showLottie },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .safeDrawingPadding()
                .padding(16.dp)
        ) {
            Crossfade(
                targetState = showLottie
            ) {
                Image(
                    imageVector = if (it) Icons.Outlined.Animation else Icons.Outlined.Draw,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun GradientAnimation(
    gradientAnimationManager: GradientAnimationManager,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .then(gradientAnimationManager.canvasModifier)

        ) {
            val originalWidth = 1920f
            val originalHeight = 1080f
            val scaleX = size.width / originalWidth
            val scaleY = size.height / originalHeight

            gradientAnimationManager.drawBackground(this)

            withTransform({
                scale(scaleX, scaleY, pivot = Offset.Zero)
            }) {
                gradientAnimationManager.drawCircles(this)
            }
        }
    }
}

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun GradientAnimationScreenPreview() {
    GradientAnimationScreen()
}

@Composable
private fun LottieReference() {
    val backgroundComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.uniglow)
    )
    val backgroundProgress by animateLottieCompositionAsState(
        composition = backgroundComposition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = backgroundComposition,
        progress = { backgroundProgress },
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxSize()
    )
}