package com.example.composepractice.ui.project.gradient

import android.graphics.BlurMaskFilter
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
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
internal fun GradientAnimation() {
    var showLottie by remember { mutableStateOf(false) }

    Box {
        Crossfade(
            targetState = showLottie
        ) {
            if (it) {
                LottieReference()
            } else {
                val infiniteTransition = rememberInfiniteTransition()
                val circle1 = rememberGradientCircleAnimationState(
                    circle = GradientCircles[0],
                    infiniteTransition = infiniteTransition
                )
                
                val circle2 = rememberGradientCircleAnimationState(
                    circle = GradientCircles[1],
                    infiniteTransition = infiniteTransition
                )

                val circle3 = rememberGradientCircleAnimationState(
                    circle = GradientCircles[2],
                    infiniteTransition = infiniteTransition
                )

                val circle4 = rememberGradientCircleAnimationState(
                    circle = GradientCircles[3],
                    infiniteTransition = infiniteTransition
                )

                val circle5 = rememberGradientCircleAnimationState(
                    circle = GradientCircles[4],
                    infiniteTransition = infiniteTransition
                )


                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val originalWidth = 1920f
                    val originalHeight = 1080f
                    val scaleX = size.width / originalWidth
                    val scaleY = size.height / originalHeight

                    withTransform({
                        scale(scaleX, scaleY, pivot = Offset.Zero)
                    }) {
                        
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawCircle(
                                circle1.x.value,
                                circle1.y.value,
                                (circle1.circle.size / 2f) * circle1.scale.value,
                                circle1.circle.paint
                            )

                            circle2.let { circle ->
                                canvas.nativeCanvas.drawCircle(
                                    circle.x.value,
                                    circle.y.value,
                                    (circle.circle.size / 2f) * circle.scale.value,
                                    circle.circle.paint
                                )
                            }

                            circle3.let { circle ->
                                canvas.nativeCanvas.drawCircle(
                                    circle.x.value,
                                    circle.y.value,
                                    (circle.circle.size / 2f) * circle.scale.value,
                                    circle.circle.paint
                                )
                            }
                            
                            circle4.let { circle ->
                                canvas.nativeCanvas.drawCircle(
                                    circle.x.value,
                                    circle.y.value,
                                    (circle.circle.size / 2f) * circle.scale.value,
                                    circle.circle.paint
                                )
                            }

                            circle5.let { circle ->
                                canvas.nativeCanvas.drawCircle(
                                    circle.x.value,
                                    circle.y.value,
                                    (circle.circle.size / 2f) * circle.scale.value,
                                    circle.circle.paint
                                )
                            }
                        }
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
                    imageVector = if (it) Icons.Outlined.Animation else Icons.Outlined.Draw,
                    contentDescription = null
                )
            }
        }
    }
}

private const val MS_PER_FRAME = 1000f / 24 // 24 fps
// 500 is almost equivalent to the 1000 value (source, gemini)
private val BLUR_MASK_FILTER = BlurMaskFilter(500f, BlurMaskFilter.Blur.NORMAL)

fun KeyframesSpec.KeyframesSpecConfig<Float>.buildFromSteps(
    steps: List<AnimationStep>,
    offset: Float = 0f
) {
    durationMillis = (steps.last().frame * MS_PER_FRAME).toInt()

    steps.forEach { (value, frame, easing) ->
        val timeStamp = (frame * MS_PER_FRAME).toInt()
        value - (offset) at timeStamp using easing
    }
}

@Immutable
data class GradientCircle(
    val size: Float,
    val xOffset: Float,
    val yOffset: Float,
    @param:ColorInt val color: Int,
    val xAnimationSteps: List<AnimationStep>,
    val yAnimationSteps: List<AnimationStep>,
    val scaleAnimationSteps: List<AnimationStep>
) {
    val paint = Paint().asFrameworkPaint().apply {
        color = this@GradientCircle.color
        isAntiAlias = true
        maskFilter = BLUR_MASK_FILTER
    }
}

data class GradientCircleAnimationState(
    val circle: GradientCircle,
    val x: State<Float>,
    val y: State<Float>,
    val scale: State<Float>
)

@Composable
fun rememberGradientCircleAnimationState(
    circle: GradientCircle,
    infiniteTransition: InfiniteTransition
): GradientCircleAnimationState {
    val circleX = infiniteTransition.animateFloat(
        initialValue = circle.xAnimationSteps.first().value,
        targetValue = circle.xAnimationSteps.last().value,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                buildFromSteps(
                    steps = circle.xAnimationSteps,
                    offset = circle.xOffset
                )
            }
        )
    )

    val circleY = infiniteTransition.animateFloat(
        initialValue = circle.yAnimationSteps.first().value,
        targetValue = circle.yAnimationSteps.last().value,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                buildFromSteps(
                    steps = circle.yAnimationSteps,
                    offset = circle.yOffset
                )
            }
        )
    )

    val circleScale = infiniteTransition.animateFloat(
        initialValue = circle.scaleAnimationSteps.first().value,
        targetValue = circle.scaleAnimationSteps.last().value,
        animationSpec = infiniteRepeatable(
            animation = keyframes { buildFromSteps(circle.scaleAnimationSteps) }
        )
    )

    return remember(circle) {
        GradientCircleAnimationState(
            circle = circle,
            x = circleX,
            y = circleY,
            scale = circleScale
        )
    }
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
        progress = { backgroundProgress },
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxSize()
    )
}