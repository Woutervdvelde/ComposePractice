package com.example.composepractice.ui.project.scratchticketv2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composepractice.R
import com.example.composepractice.ui.theme.FramnaGreen

@Composable
internal fun ScratchTicketV2() {
    val scratchState = rememberScratchState(
        scratchStrokeWidth = with(LocalDensity.current) { 25.dp.toPx() },
        onThresholdReached = { progress, scratchState ->
            scratchState.reveal(showAnimation = true)
        },
        onRevealed = {}
    )


    Scaffold { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .onGloballyPositioned { layoutCoordinates ->
                    scratchState.externalLayoutCoordinates = layoutCoordinates
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = scratchState::handleExternalDrag,
                        onDragEnd = scratchState::handleExternalDragEnd
                    )
                }
        ) {
            ScratchTicket(
                modifier = Modifier
                    .width(400.dp)
                    .height(250.dp),
                scratchState = scratchState,
                scratchContent = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(FramnaGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.framna),
                            contentDescription = null
                        )
                    }
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Yellow)
                ) {
                    Text("\uD83C\uDFC6", fontSize = 100.sp) // Trophy emoji
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = scratchState::reset
                ) {
                    Text("Reset ticket")
                }
                Button(
                    onClick = { scratchState.reveal(showAnimation = true) }
                ) {
                    Text("Animated reveal")
                }
                Button(
                    onClick = { scratchState.reveal(showAnimation = false) }
                ) {
                    Text("Reveal")
                }
            }

            Text(text = "Revealed: ${scratchState.isRevealed.value}")
        }
    }
}