package com.example.composepractice.ui.project.zipper

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.composepractice.R
import com.example.composepractice.ui.theme.ComposePracticeTheme

@Composable
fun ZipperScreen() {
    val resources = LocalResources.current
    val denimTexture = BitmapFactory
        .decodeResource(resources, R.drawable.denim_texture)
        .asImageBitmap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .drawWithContent {
                val path = Path().apply {
                    moveTo(x = size.width / 2f, y = 0f)
                    cubicTo(
                        size.width / 4f, size.height / 2f,
                        size.width - (size.width / 4f), size.height / 2f,
                        size.width / 2f, size.height
                    )
                }
                val pathMeasure = PathMeasure().apply {
                    setPath(path = path, forceClosed = false)
                }

                drawContent()

                drawIntoCanvas { canvas ->
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(
                            width = 2.dp.toPx()
                        )
                    )

                    val strokeHeight = 1 // Defines the definition of each stroke height
                    val width = size.width / 2f
                    for (y in 1..size.height.toInt() / strokeHeight) {
                        val x = pathMeasure
                            .getPosition(y.toFloat()).x // Current x offset along path

                        val srcOffset = IntOffset(
                            x = width.toInt(),
                            y = y
                        )
                        val srcSize = IntSize(
                            width = denimTexture.width,
                            height = strokeHeight
                        )
                        val dstSize = srcSize
                        val dstOffset = IntOffset(
                            x = x.toInt(),
                            y = y
                        )

                        drawImage(
                            image = denimTexture,
                            srcSize = srcSize,
                            srcOffset = srcOffset,
                            dstOffset = dstOffset,
                            dstSize = dstSize
                        )
                    }
                }

            }
    ) {

    }
}


@Preview
@Composable
private fun ZipperScreenPreview() {
    ComposePracticeTheme {
        ZipperScreen()
    }
}