package com.example.composepractice.ui.project.compositingstrategy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.trace

@Composable
fun CompositingStrategyScreen() {
    Scaffold { scaffoldPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
//                .verticalScroll(rememberScrollState()),
        ) {
//            H1("Default overlapping blocks")
//            CompositingBlock(CompositingStrategy.Auto, "Auto")
//            CompositingBlock(CompositingStrategy.Offscreen, "Offscreen")
            CompositingBlock(CompositingStrategy.ModulateAlpha, "ModulateAlpha")

//            H1("Single canvas using two draw calls")
//            CustomVectorBugTest(CompositingStrategy.Auto)
//            CustomVectorBugTest(CompositingStrategy.Offscreen)
//            CustomVectorBugTest(CompositingStrategy.ModulateAlpha)
        }
    }
}

@Composable
private fun CompositingBlock(strategy: CompositingStrategy, text: String) {
    trace("CompositingBlock: $text") {
        Box(
            modifier = Modifier.graphicsLayer {
                this.alpha = .5f
                this.compositingStrategy = strategy
            },
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.Red),
            )
            Text(text = text, color = Color.Blue, fontWeight = FontWeight.Black, fontSize = 48.sp)
        }
    }
}

@Composable
private fun CustomVectorBugTest(strategy: CompositingStrategy) {
    Canvas(
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer {
                this.alpha = 0.5f
                this.compositingStrategy = strategy
            }
    ) {
        drawRect(
            color = Color.Red,
            topLeft = Offset(x = 20.dp.toPx(), y = 80.dp.toPx()),
            size = Size(width = 160.dp.toPx(), height = 40.dp.toPx())
        )

        drawRect(
            color = Color.Red,
            topLeft = Offset(x = 80.dp.toPx(), y = 20.dp.toPx()),
            size = Size(width = 40.dp.toPx(), height = 160.dp.toPx())
        )
    }
}

@Composable
private fun H1(text: String) {
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 32.dp),
    )
}