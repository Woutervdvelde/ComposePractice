package com.example.composepractice.ui.project.aiaccidentdrawing.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.composepractice.ui.project.aiaccidentdrawing.AiAccidentEngine
import kotlinx.coroutines.launch

@Composable
internal fun DrawingScreen(engine: AiAccidentEngine) {
    var text by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column {
        TextField(
            value = text,
            onValueChange = { text = it },
        )
        Button(
            onClick = {
                response = ""
                scope.launch {
                    engine.sendMessage(text).collect { response += it }
                }
            },
        ) { Text("Send") }
        
        Text(text = response)
    }
}