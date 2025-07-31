package com.example.composepractice.ui.project.rive

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import com.example.composepractice.R

@Composable
fun RiveScreen() {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            val stateMachine = "StateMachine"
            var isPlaying by remember { mutableStateOf(true) }
            var isLoading by remember { mutableStateOf(true) }
            var isConnected by remember { mutableStateOf(false) }
            val animations by remember { mutableStateOf(listOf<String>()) }

            RiveAnimation(
                resId = R.raw.rive_connection,
                stateMachine = stateMachine,
                onInit = { view ->
                    view.setBooleanState(stateMachine, "isLoading", isLoading)
                    view.setBooleanState(stateMachine, "isConnected", isConnected)

                    if (isPlaying) view.play()
                    else view.pause()
                }
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    Button(
                        onClick = { isPlaying = !isPlaying }
                    ) { Text(if (isPlaying) "pause" else "play") }
                }

                Column {
                    Button(
                        onClick = { isLoading = !isLoading }
                    ) { Text("Loading") }
                    Text("$isLoading")
                }

                Column {
                    Button(
                        onClick = { isConnected = !isConnected }
                    ) { Text("Connected") }
                    Text("$isConnected")
                }
            }
            Column {
                Text("Animations:", style = MaterialTheme.typography.headlineMedium)
                animations.forEach { Text("\t- $it") }
            }
        }
    }
}

/**
 * Statmachine -> view.stateMachines (list)
 * Inputs -> stateMachine.inputs
 */
@Composable
fun RiveAnimation(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    stateMachine: String? = null,
    onInit: (RiveAnimationView) -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            RiveAnimationView(context).apply {
                setRiveResource(
                    resId = resId,
                    stateMachineName = stateMachine
                )
            }
        },
        update = { view -> onInit(view) }
    )
}