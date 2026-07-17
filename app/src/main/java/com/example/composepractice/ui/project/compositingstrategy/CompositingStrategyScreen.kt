package com.example.composepractice.ui.project.compositingstrategy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompositingStrategyScreen() {
    Scaffold { scaffoldPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
        ) {
            CompositingBlock(CompositingStrategy.Auto, "Auto")
            CompositingBlock(CompositingStrategy.Offscreen, "Offscreen")
            CompositingBlock(CompositingStrategy.ModulateAlpha, "ModulateAlpha")
        }
    }
}

@Composable
private fun CompositingBlock(strategy: CompositingStrategy, text: String) {
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = .5f
            this.compositingStrategy = strategy
        }
    ) {
        Box(modifier = Modifier.size(200.dp).background(Color.Red))
        Text(text = text, color = Color.Blue, fontWeight = FontWeight.Black, fontSize = 48.sp)
    }
}