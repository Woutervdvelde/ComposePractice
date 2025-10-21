package com.example.composepractice.ui.project.counter

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CardProperties(
    val cardSize: DpSize = DpSize(64.dp, 80.dp),
    val cardBackground: Color = Color(0xFFF5F5F5),
    val ringSize: DpSize = DpSize(4.dp, 20.dp),
    val textStyle: TextStyle = TextStyle.Default.copy(fontSize = 48.sp, lineHeight = 48.sp)
)

/**
 * @property size Size of the ring
 * @property yOffset yOffset of the ring, by default aligned vertically to the top of the card
 */
data class RingProperties(
    val size: DpSize = DpSize(4.dp, 20.dp),
    val yOffset: Dp = 2.dp,
    val brush: Brush = SolidColor(Color.Red),
    val shape: Shape = RoundedCornerShape(20.dp)
) {
    fun getFinalYOffset() = (size.height / 2) - yOffset
}

@Composable
fun Counter(
    modifier: Modifier = Modifier,
    cardProperties: CardProperties = CardProperties(),
    ringProperties: RingProperties = RingProperties()
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        label = "rotation",
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = 1000)
        )
    )

    Row(modifier = modifier) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(top = ringProperties.getFinalYOffset())
        ) {
            CounterRing(
                ringProperties = ringProperties
            )

            SingleCounter(
                value = 1,
                cardProperties = cardProperties,
                ringProperties = ringProperties,
                modifier = Modifier.graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    rotationX = rotation
                }
            )

//            SingleCounter(
//                value = 2,
//                cardProperties = cardProperties,
//                ringProperties = ringProperties
//            )


        }
    }
}


@Composable
private fun CounterRing(
    ringProperties: RingProperties,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .size(ringProperties.size)
            .offset {
                IntOffset(
                    x = 0,
                    y = -(ringProperties.getFinalYOffset()).roundToPx()
                )
            }
            .background(ringProperties.brush, ringProperties.shape)
    )
}

@Composable
private fun SingleCounter(
    value: Int,
    cardProperties: CardProperties,
    ringProperties: RingProperties,
    modifier: Modifier = Modifier
) {
    val circleRadius = (ringProperties.size.width + 8.dp) / 2
    val circleOffsetY = ringProperties.size.height / 2 + ringProperties.yOffset

    val textMeasurer = rememberTextMeasurer()
    val counterText = textMeasurer.measure(
        text = value.toString(),
        style = cardProperties.textStyle
    )

    Canvas(
        modifier = modifier
            .graphicsLayer(alpha = .99f) // Otherwise won't show transparency
            .size(cardProperties.cardSize)
    ) {
        val ringWidthPx = cardProperties.ringSize.width.toPx()

        val circleCenterX = size.width / 2
        val circleCenterY = circleOffsetY.toPx()
        val cornerRadius = 8.dp.toPx()

        drawIntoCanvas { canvas ->
            canvas.save()

            drawRoundRect(
                color = cardProperties.cardBackground,
                size = size,
                cornerRadius = CornerRadius(cornerRadius)
            )

            drawText(
                textLayoutResult = counterText,
                topLeft = Offset(
                    x = circleCenterX - (counterText.size.width / 2),
                    y = size.height / 2 - (counterText.size.height / 2) + (circleCenterY - circleRadius.toPx() / 2)
                )
            )

            // Clipping
            val clearPaint = Paint().apply { blendMode = BlendMode.Clear }
            canvas.drawCircle(
                center = Offset(x = circleCenterX, y = circleCenterY),
                radius = circleRadius.toPx(),
                paint = clearPaint
            )

            canvas.drawRect(
                rect = Rect(
                    offset = Offset(
                        x = circleCenterX - (ringWidthPx / 2),
                        y = 0f
                    ),
                    size = Size(
                        width = ringWidthPx,
                        height = circleOffsetY.toPx()
                    )
                ),
                paint = clearPaint
            )

            canvas.restore()
        }
    }
}

@Composable
private fun CounterPreview() {
    Column {
        Counter()
    }
}