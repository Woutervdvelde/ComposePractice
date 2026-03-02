package com.example.composepractice.ui.project.gradient

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.example.composepractice.ui.project.gradient.GradientAnimationManager.Companion.buildFromSteps

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