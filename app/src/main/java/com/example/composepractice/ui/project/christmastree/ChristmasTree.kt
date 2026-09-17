package com.example.composepractice.ui.project.christmastree

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.composepractice.R
import kotlin.random.Random
import kotlin.random.nextInt

private val TREE_WIDTH_REFERENCE = 300.dp
private val TREE_HEIGHT_REFERENCE = 417.dp
private val ORNAMENT_PLACEHOLDER_SIZE = 48.dp
private val ORNAMENT_SIZE = DpSize(48.dp, 60.dp)

private val ORNAMENT_POSITIONS = mapOf<Int, DpOffset>(
    0 to DpOffset(151.dp, 53.dp),
    1 to DpOffset(79.dp, 87.dp),
    2 to DpOffset(130.dp, 131.dp),
    3 to DpOffset(196.dp, 168.dp),
    4 to DpOffset(44.dp, 192.dp),
    5 to DpOffset(121.dp, 230.dp),
    6 to DpOffset(223.dp, 240.dp),
    7 to DpOffset(62.dp, 278.dp),
    8 to DpOffset(175.dp, 294.dp),
    9 to DpOffset((-17).dp, 312.dp),
    10 to DpOffset(277.dp, 319.dp),
)

@Composable
fun ChristmasTree(
    tree: ChristmasTreeData,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(TREE_WIDTH_REFERENCE)
            .aspectRatio(TREE_WIDTH_REFERENCE / TREE_HEIGHT_REFERENCE),
    ) {
        Image(
            painter = painterResource(R.drawable.christmas_tree),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
        )

        ORNAMENT_POSITIONS.forEach { (i, offset) ->
            when (val collected = tree.ornaments.get(i)) {
                null -> OrnamentPlaceholder(position = offset)
                else -> Ornament(type = collected, index = i, position = offset)
            }
        }
    }
}

@Composable
fun Ornament(type: OrnamentType, index: Int, position: DpOffset) {
    val color = when (type) {
        OrnamentType.HOUSE -> Color(0xFFE57373)          // Soft Red
        OrnamentType.BICYCLE -> Color(0xFFFFB74D)        // Bright Orange
        OrnamentType.HEART -> Color(0xFFF06292)          // Hot Pink
        OrnamentType.CYBER_SHIELD -> Color(0xFF00E676)   // Cyber Neon Green
        OrnamentType.MITTENS -> Color(0xFFBA68C8)        // Purple
        OrnamentType.PAW -> Color(0xFFA1887F)            // Warm Brown
        OrnamentType.CAR -> Color(0xFF64B5F6)            // Sky Blue
        OrnamentType.SUITCASE -> Color(0xFFFFD54F)       // Yellow / Gold
        OrnamentType.SPORT_SHOE -> Color(0xFFFF7043)     // Coral
        OrnamentType.STREET_LANTERN -> Color(0xFF4DD0E1) // Cyan
        OrnamentType.STAR -> Color(0xFFFFEB3B)           // Vivid Yellow
    }
    
    val transition = rememberInfiniteTransition()
    val durationMs = 2000
    val bezierEasing = CubicBezierEasing(0.5f, 0f, 0.5f, 1f)
    val rotation by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = bezierEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(
                offsetMillis = Random(index).nextInt(0, durationMs),
                offsetType = StartOffsetType.FastForward
            )
        )
    )
    
    Box(
        modifier = Modifier
            .size(ORNAMENT_SIZE)
            .offset(x = position.x, y = position.y)
            .graphicsLayer { 
                transformOrigin = TransformOrigin(0.5f, 0f)
                rotationZ = rotation
            }
            .drawBehind {
                drawOval(
                    color = color,
                )
            }
    )
}

@Composable
fun OrnamentPlaceholder(position: DpOffset) {
    Box(
        modifier = Modifier
            .size(ORNAMENT_PLACEHOLDER_SIZE)
            .offset(x = position.x, y = position.y)
            .background(Color.Black.copy(alpha = .4f), CircleShape)
            .drawWithContent {
                drawContent()
                drawCircle(
                    color = Color.White,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(5.dp.toPx(), 5.dp.toPx()),
                        ),
                    ),
                )
            },
    )
}

@Preview
@Composable
private fun ChristmasTreePreview() {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 32.dp)
    ) {
        ChristmasTree(
            tree = dummyTree,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}