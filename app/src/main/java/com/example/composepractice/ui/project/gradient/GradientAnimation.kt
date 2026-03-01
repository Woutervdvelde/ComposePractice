package com.example.composepractice.ui.project.gradient

import android.graphics.RenderEffect
import android.graphics.Shader
import android.renderscript.RenderScript
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.composepractice.R


@Composable
internal fun GradientAnimation() {
    var showLottie by remember { mutableStateOf(false) }

    Box {
        Crossfade(
            targetState = showLottie
        ) {
            if (it) {
                LottieReference()
            } else {
                                val circle1XSteps = listOf(
                    AnimationStep(39.11f, 0, CubicBezierEasing(0.259f, -5.405f, 0.474f, -0.643f)),
                    AnimationStep(97.1f, 60, CubicBezierEasing(0.459f, -0.567f, 0.744f, 0.69f)),
                    AnimationStep(1.66f, 99, CubicBezierEasing(0.526f, -0.093f, 0.488f, 0.942f)),
                    AnimationStep(1158.5f, 168, CubicBezierEasing(0.36f, -0.146f, 0.696f, 0.273f)),
                    AnimationStep(1031f, 195, CubicBezierEasing(0.525f, 0.262f, 0.895f, 0.836f)),
                    AnimationStep(39.11f, 240, LinearEasing),
                )

                val circle1YSteps = listOf(
                    AnimationStep(-586.53f, 0, CubicBezierEasing(0.004f, 0f, 0.746f, 0.133f)),
                    AnimationStep(619.34f, 52, CubicBezierEasing(0.195f, 1.046f, 0.609f, 1.204f)),
                    AnimationStep(1312.61f, 99, CubicBezierEasing(0.268f, 0.077f, 0.637f, 1.202f)),
                    AnimationStep(-533.09f, 168, CubicBezierEasing(0.363f, 0.581f, 0.699f, 0.881f)),
                    AnimationStep(-281.26f, 195, CubicBezierEasing(0.614f, -0.326f, 0.996f, 1f)),
                    AnimationStep(-586.53f, 240, LinearEasing),
                )

                val fps = 24
                val msPerFrame = 1000f / fps

                fun KeyframesSpec.KeyframesSpecConfig<Float>.buildFromSteps(steps: List<AnimationStep>) {
                    durationMillis = (steps.last().frame * msPerFrame).toInt()

                    steps.forEach { (value, frame, easing) ->
                        val timeStamp = (frame * msPerFrame).toInt()
                        value at timeStamp using easing
                    }
                }

                val infiniteTransition = rememberInfiniteTransition()
                val circle1X by infiniteTransition.animateFloat(
                    initialValue = circle1XSteps.first().value,
                    targetValue = circle1XSteps.last().value,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes { buildFromSteps(circle1XSteps) }
                    )
                )

                val circle1Y by infiniteTransition.animateFloat(
                    initialValue = circle1YSteps.first().value,
                    targetValue = circle1YSteps.last().value,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes { buildFromSteps(circle1YSteps) }
                    )
                )

                val circle1 = GradientCircle(
                    bitmap = ImageBitmap.imageResource(id = R.drawable.circle1_v2),
                    originalSize = 914f,
                    offsetX = -196.89f,
                    offsetY = -199.11f
                )

                         Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            renderEffect = RenderEffect.createBlurEffect(
                                550f,
                                550f,
                                Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        }
                ) {
                    val originalWidth = 1920f
                    val originalHeight = 1080f
                    val scaleX = size.width / originalWidth
                    val scaleY = size.height / originalHeight

                    withTransform({
                        scale(scaleX, scaleY, pivot = Offset.Zero)
                    }) {
                        val centerX = circle1X - (circle1.offsetX)
                        val centerY = circle1Y - (circle1.offsetY)
                        val centerOffset = Offset(centerX, centerY)

                        val animationScale = 1.52f
                        val baseRadius = (circle1.originalSize / 2f)
                        val finalRadius = baseRadius * animationScale

                        val brush = Brush.radialGradient(
                            0.0f to Color(0xFF125C30),
                            1.0f to Color(0xFF125C30),
                            center = centerOffset,
                            radius = finalRadius
                        )

                        drawCircle(
                            brush = brush,
                            center = centerOffset,
                            radius = finalRadius
                        )
                    }
                }
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
                    imageVector = if (it) Icons.Outlined.Draw else Icons.Outlined.Animation,
                    contentDescription = null
                )
            }
        }
    }
}

data class GradientCircle(
    val bitmap: ImageBitmap,
    val originalSize: Float,
    val offsetX: Float,
    val offsetY: Float
) {
    val size = bitmap.width
    val scale = originalSize / size
}

data class AnimationStep(
    val value: Float,
    val frame: Int,
    val easing: Easing
)

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun GradientAnimationPreview() {
    GradientAnimation()
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
        progress = backgroundProgress,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxSize()
    )
}