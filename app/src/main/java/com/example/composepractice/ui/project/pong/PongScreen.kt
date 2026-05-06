package com.example.composepractice.ui.project.pong

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.composepractice.R
import com.example.composepractice.ui.util.LockScreenOrientation
import com.example.composepractice.ui.util.Orientation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PongScreen() {
    var aiScore by remember { mutableIntStateOf(0) }
    var playerScore by remember { mutableIntStateOf(0) }
    var showScore by remember { mutableStateOf(false) }

    var delayStart: suspend () -> Unit = {}
    val coroutineScope = rememberCoroutineScope()
    val pongState = remember {
        PongState(
            playerWidth = 300f,
            onScored = { scored ->
                when (scored) {
                    PongState.Companion.Scored.PLAYER -> playerScore++
                    PongState.Companion.Scored.AI -> aiScore++
                }

                coroutineScope.launch {
                    delayStart()
                }
            }
        )
    }

    delayStart = {
        pongState.pause()
        showScore = true
        delay(2000L)
        showScore = false
        delay(500L)
        pongState.play()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        val resources = LocalResources.current
        val (bitmapRight, bitmapLeft) = remember(resources) {
            val original = BitmapFactory.decodeResource(resources, R.drawable.unive_banner)

            fun rotate(degrees: Float): ImageBitmap {
                val matrix = Matrix().apply {
                    postRotate(degrees)
                }

                val rotated = Bitmap.createBitmap(
                    original,
                    0,
                    0,
                    original.width,
                    original.height,
                    matrix,
                    true
                )

                return rotated.asImageBitmap()
            }

            rotate(90f) to rotate(-90f)
        }
        val horizontalPadding = 24.dp
        val horizontalPaddingPx = with(LocalDensity.current) { horizontalPadding.toPx() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF125C30))
                .drawWithContent {
                    // Draw circle in center 1/4 of the width with a stroke of 5f
                    drawCircle(
                        color = Color(0xFFC2C9C5),
                        radius = size.minDimension / 4,
                        center = Offset(size.width / 2, size.height / 2),
                        style = Stroke(
                            width = 10f
                        )
                    )
                    drawCircle(
                        color = Color(0xFFC2C9C5),
                        radius = 20f,
                        center = Offset(size.width / 2, size.height / 2),
                    )
                    drawLine(
                        color = Color(0xFFC2C9C5),
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = 10f
                    )

                    val bannerWidth = horizontalPaddingPx
                    val scale = bannerWidth / bitmapLeft.width

                    val leftBrush = ShaderBrush(
                        ImageShader(bitmapLeft, TileMode.Clamp, TileMode.Repeated)
                    )

                    val rightBrush = ShaderBrush(
                        ImageShader(bitmapRight, TileMode.Clamp, TileMode.Repeated)
                    )

                    withTransform({
                        scale(scale, scale, pivot = Offset.Zero)
                    }) {
                        drawRect(
                            brush = leftBrush,
                            topLeft = Offset(0f, 0f),
                            size = Size(
                                bitmapLeft.width.toFloat(),
                                size.height / scale
                            )
                        )
                    }

                    withTransform({
                        scale(scale, scale, pivot = Offset.Zero)
                        translate(
                            (size.width - bannerWidth) / scale,
                            0f
                        )
                    }) {
                        drawRect(
                            brush = rightBrush,
                            topLeft = Offset(0f, 0f),
                            size = Size(
                                bitmapRight.width.toFloat(),
                                size.height / scale
                            )
                        )
                    }

                    drawContent()
                }
        ) {
            Image(
                painter = painterResource(R.drawable.goalpostv2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
//                    .rotate(180f)
            )
            Pong(
                pongState = pongState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = horizontalPadding),
            )
            Image(
                painter = painterResource(R.drawable.goalpost),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = showScore,
            enter = slideInHorizontally(),
            exit = slideOutHorizontally { it },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$playerScore - $aiScore",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 50.dp)
            )
        }
    }

    LockScreenOrientation(orientation = Orientation.PORTRAIT)
}
