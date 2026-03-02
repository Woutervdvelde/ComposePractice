package com.example.composepractice.ui.project.gradient

import android.graphics.BlurMaskFilter
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import com.example.composepractice.ui.project.gradient.GradientAnimationManager.Companion.MS_PER_FRAME

@Composable
fun rememberGradientAnimationManager(
    gradientCircles: List<GradientCircle>,
    infiniteTransition: InfiniteTransition
): GradientAnimationManager {
    val circleStates = gradientCircles.map {
        rememberGradientCircleAnimationState(
            circle = it,
            infiniteTransition = infiniteTransition
        )
    }

    val backgroundColor = infiniteTransition.animateColor(
        initialValue = Color(0xFF0D603D),
        targetValue = Color(0xFF0D603D),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = (239 * MS_PER_FRAME).toInt()

                Color(0xFF0D603D) at (0 * MS_PER_FRAME).toInt() using CubicBezierEasing(
                    0.01f,
                    0f,
                    0.667f,
                    1.054f
                )
                Color(0xFF005732) at (26 * MS_PER_FRAME).toInt() using CubicBezierEasing(
                    0.333f,
                    0.059f,
                    0.667f,
                    1f
                )
                Color(0xFF279968) at (164 * MS_PER_FRAME).toInt() using CubicBezierEasing(
                    0.333f,
                    0f,
                    0.667f,
                    1f
                )
                Color(0xFF0D603D) at (239 * MS_PER_FRAME).toInt() using LinearEasing
            }
        )
    )


    return remember(gradientCircles) {
        GradientAnimationManager(
            circleStates = circleStates,
            backgroundColor = backgroundColor
        )
    }
}

class GradientAnimationManager(
    val circleStates: List<GradientCircleAnimationState>,
    val backgroundColor: State<Color>
) {
    // RenderEffect is only available for Android >= 12
    private val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val canvasModifier: Modifier
        get() = if (isBlurSupported) {
            Modifier.graphicsLayer {
                renderEffect = android.graphics.RenderEffect.createBlurEffect(
                    250f, 250f, Shader.TileMode.MIRROR
                ).asComposeRenderEffect()
            }
        } else Modifier

    fun drawCircles(scope: DrawScope) {
        scope.drawIntoCanvas { canvas ->
            circleStates.forEach { (circle, x, y, scale) ->
                canvas.nativeCanvas.drawCircle(
                    x.value,
                    y.value,
                    (circle.size / 2f) * scale.value,
                    circle.paint.apply {
                        if (!isBlurSupported) {
                            maskFilter = BLUR_MASK_FILTER
                        }
                    }
                )
            }
        }
    }

    fun drawBackground(scope: DrawScope) {
        scope.drawRect(color = backgroundColor.value)
    }

    companion object {
        const val MS_PER_FRAME = 1000f / 24 // 24 fps
        private val BLUR_MASK_FILTER = BlurMaskFilter(500f, BlurMaskFilter.Blur.NORMAL)

        fun KeyframesSpec.KeyframesSpecConfig<Float>.buildFromSteps(
            steps: List<AnimationStep>,
        ) {
            durationMillis = (steps.last().frame * MS_PER_FRAME).toInt()

            steps.forEach { (value, frame, easing) ->
                val timeStamp = (frame * MS_PER_FRAME).toInt()
                value at timeStamp using easing
            }
        }
    }
}