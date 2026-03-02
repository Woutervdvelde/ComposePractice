package com.example.composepractice.ui.project.gradient

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing

val GradientCircles = listOf(
    // Dark 1
    GradientCircle(
        size = 913.781f,
        xOffset = -196.89f,
        yOffset = -199.11f,
        color = 0xFF125C30.toInt(),
        xAnimationSteps = listOf(
            AnimationStep(39.11f, 0, CubicBezierEasing(0.259f, -5.405f, 0.474f, -0.643f)),
            AnimationStep(97.1f, 60, CubicBezierEasing(0.459f, -0.567f, 0.744f, 0.69f)),
            AnimationStep(1.66f, 99, CubicBezierEasing(0.526f, -0.093f, 0.488f, 0.942f)),
            AnimationStep(1158.5f, 168, CubicBezierEasing(0.36f, -0.146f, 0.696f, 0.273f)),
            AnimationStep(1031f, 195, CubicBezierEasing(0.525f, 0.262f, 0.895f, 0.836f)),
            AnimationStep(39.11f, 239, LinearEasing),
        ),
        yAnimationSteps = listOf(
            AnimationStep(-586.53f, 0, CubicBezierEasing(0.004f, 0f, 0.746f, 0.133f)),
            AnimationStep(619.34f, 52, CubicBezierEasing(0.195f, 1.046f, 0.609f, 1.204f)),
            AnimationStep(1312.61f, 99, CubicBezierEasing(0.268f, 0.077f, 0.637f, 1.202f)),
            AnimationStep(-533.09f, 168, CubicBezierEasing(0.363f, 0.581f, 0.699f, 0.881f)),
            AnimationStep(-281.26f, 195, CubicBezierEasing(0.614f, -0.326f, 0.996f, 1f)),
            AnimationStep(-586.53f, 239, LinearEasing),
        ),
        scaleAnimationSteps = listOf(
            AnimationStep(1.52f, 0, CubicBezierEasing(0.333f, 0f, 0.667f, 1f)),
            AnimationStep(1.5f, 40, CubicBezierEasing(0.333f, 0f, 0.667f, 1f)),
            AnimationStep(1.52f, 239, LinearEasing)
        )
    ),

    // Light 1
    GradientCircle(
        size = 913.78f,
        xOffset = 412.48f,
        yOffset = 811.28f,
        color = 0xFF00CFAE.toInt(),
        xAnimationSteps = listOf(
            AnimationStep(1508.11f, 0, CubicBezierEasing(0.693f, 0f, 0.744f, 1.056f)),
            AnimationStep(-111.48f, 73, CubicBezierEasing(0.527f, 0.111f, 0.67f, 0.866f)),
            AnimationStep(2001.37f, 164, CubicBezierEasing(0.395f, -0.566f, 0.514f, 1f)),
            AnimationStep(1508.11f, 239, LinearEasing)
        ),
        yAnimationSteps = listOf(
            AnimationStep(180.80f, 0, CubicBezierEasing(0.693f, 0f, 0.672f, 1.723f)),
            AnimationStep(729.04f, 59, CubicBezierEasing(0.331f, 0.453f, 0.415f, -0.19f)),
            AnimationStep(10.14f, 107, CubicBezierEasing(0.227f, -1.493f, 0.419f, 0.599f)),
            AnimationStep(366.70f, 184, CubicBezierEasing(0.419f, -0.397f, 0.514f, 1f)),
            AnimationStep(180.80f, 239, LinearEasing)
        ),
        scaleAnimationSteps = listOf(
            AnimationStep(1.0f, 6, CubicBezierEasing(0.333f, 0f, 0.667f, 1f)),
            AnimationStep(1.50f, 94, CubicBezierEasing(0.333f, 0f, 0.667f, 1f)),
            AnimationStep(1.0f, 239, LinearEasing)
        )
    ),

    // Medium
    GradientCircle(
        size = 913.78f,
        xOffset = 412.48f,
        yOffset = 811.28f,
        color = 0xFF125C30.toInt(),
        xAnimationSteps = listOf(
            AnimationStep(1796.0f, 0, CubicBezierEasing(0.26f, 0.984f, 0.804f, 2.971f)),
            AnimationStep(1715.03f, 39, CubicBezierEasing(0.396f, 0.918f, 0.685f, 1.385f)),
            AnimationStep(2290.47f, 103, CubicBezierEasing(0.289f, 0.106f, 0.398f, 1.1f)),
            AnimationStep(583.66f, 160, CubicBezierEasing(0.524f, 0.141f, 0.551f, 0.239f)),
            AnimationStep(1230.64f, 195, CubicBezierEasing(0.245f, 0.597f, 0.782f, 1.264f)),
            AnimationStep(1796.0f, 239, LinearEasing)
        ),
        yAnimationSteps = listOf(
            AnimationStep(1507.75f, 0, CubicBezierEasing(0.005f, 0f, 0.777f, 0.751f)),
            AnimationStep(535.75f, 48, CubicBezierEasing(0.347f, 0.749f, 0.773f, 1.406f)),
            AnimationStep(-230.43f, 121, CubicBezierEasing(0.283f, 0.251f, 0.577f, 0.458f)),
            AnimationStep(552.41f, 158, CubicBezierEasing(0.33f, 0.541f, 0.65f, 0.891f)),
            AnimationStep(1413.56f, 210, CubicBezierEasing(0.421f, 0.669f, 0.995f, 1f)),
            AnimationStep(1507.75f, 239, LinearEasing)
        ),
        scaleAnimationSteps = listOf(
            AnimationStep(1.0f, 0, CubicBezierEasing(0.333f, 0f, 0.634f, 0.71f)),
            AnimationStep(1.94f, 51, CubicBezierEasing(0.477f, -0.931f, 0.769f, -0.225f)),
            AnimationStep(1.25f, 147, CubicBezierEasing(0.321f, 4.963f, 0.667f, 1f)),
            AnimationStep(1.0f, 239, LinearEasing)
        )
    ),

    // Light 2
    GradientCircle(
        size = 913.78f,
        xOffset = 412.48f,
        yOffset = 811.28f,
        color = 0xFF00CFAE.toInt(),
        xAnimationSteps = listOf(
            AnimationStep(184.75f, 0, CubicBezierEasing(0.693f, 0f, 0.436f, 0.762f)),
            AnimationStep(1469.26f, 46, CubicBezierEasing(0.382f, 1.924f, 0.811f, 1.962f)),
            AnimationStep(1630.70f, 115, CubicBezierEasing(0.234f, 0.14f, 0.73f, 0.696f)),
            AnimationStep(155.32f, 189, CubicBezierEasing(0.311f, -11.838f, 0.514f, 1f)),
            AnimationStep(184.75f, 239, LinearEasing)
        ),
        yAnimationSteps = listOf(
            AnimationStep(1323.53f, 0, CubicBezierEasing(0.693f, 0f, 0.657f, 0.966f)),
            AnimationStep(1570.90f, 72, CubicBezierEasing(0.455f, -0.025f, 0.721f, 0.575f)),
            AnimationStep(1107.98f, 145, CubicBezierEasing(0.163f, -0.283f, 0.477f, 0.972f)),
            AnimationStep(1520.38f, 219, CubicBezierEasing(0.628f, -0.019f, 0.514f, 1f)),
            AnimationStep(1323.53f, 239, LinearEasing)
        ),
        scaleAnimationSteps = listOf(
            AnimationStep(1.56f, 7, CubicBezierEasing(0.333f, 0f, 0.722f, 0.5f)),
            AnimationStep(1.08f, 60, CubicBezierEasing(0.249f, 0.564f, 0.667f, 1.195f)),
            AnimationStep(0.59f, 133, CubicBezierEasing(0.333f, 0.142f, 0.667f, 1f)),
            AnimationStep(1.56f, 239, LinearEasing)
        )
    ),

    // Dark 2
    GradientCircle(
        size = 913.78f,
        xOffset = -196.89f,
        yOffset = -199.11f,
        color = 0xFF125C30.toInt(),
        xAnimationSteps = listOf(
            AnimationStep(39.11f, 0, CubicBezierEasing(0.271f, -0.68f, 0.738f, 1.048f)),
            AnimationStep(532.92f, 156, CubicBezierEasing(0.58f, 0.056f, 0.567f, 1f)),
            AnimationStep(39.11f, 239, LinearEasing)
        ),
        yAnimationSteps = listOf(
            AnimationStep(-586.54f, 0, CubicBezierEasing(0.57f, -0.318f, 0.459f, 1.316f)),
            AnimationStep(214.39f, 83, CubicBezierEasing(0.607f, -0.786f, 0.735f, 1.133f)),
            AnimationStep(628.14f, 178, CubicBezierEasing(0.549f, 0.06f, 0.48f, 0.598f)),
            AnimationStep(-586.54f, 239, LinearEasing)
        ),
        scaleAnimationSteps = listOf(
            AnimationStep(1.52f, 0, CubicBezierEasing(0.333f, 0f, 0.667f, 1f)),
            AnimationStep(2.26f, 31, CubicBezierEasing(0.333f, 0f, 0.667f, 0.772f)),
            AnimationStep(1.30f, 166, CubicBezierEasing(0.333f, -0.537f, 0.667f, 1f)),
            AnimationStep(1.52f, 239, LinearEasing)
        )
    )
)