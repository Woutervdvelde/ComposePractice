package com.example.composepractice.ui.project.aiaccidentdrawing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.composepractice.ui.project.aiaccidentdrawing.AiAccidentEngine
import com.example.composepractice.ui.project.aiaccidentdrawing.AiModel
import com.example.composepractice.ui.project.aiaccidentdrawing.LocalModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface ModelAction {
    data object Idle : ModelAction
    data class Downloading(val model: AiModel, val progress: Float?) : ModelAction
    data class Loading(val model: LocalModel) : ModelAction
}

@Composable
internal fun ModelScreen(
    engine: AiAccidentEngine,
    onModelLoaded: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var action by remember { mutableStateOf<ModelAction>(ModelAction.Idle) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    val undownloadedModels = remember(refreshKey) { engine.listUndownloadedModels() }
    val downloadedModels = remember(refreshKey) { engine.listDownloadedModels() }
    val isBusy = action != ModelAction.Idle

    fun download(model: AiModel) {
        error = null
        action = ModelAction.Downloading(model, progress = null)
        scope.launch {
            try {
                var lastPercent = -1
                engine.downloadModel(model) { downloadedBytes, totalBytes ->
                    if (totalBytes <= 0) return@downloadModel
                    // Only update state once per percent, the callback fires for every buffer
                    val percent = (downloadedBytes * 100 / totalBytes).toInt()
                    if (percent != lastPercent) {
                        lastPercent = percent
                        action = ModelAction.Downloading(model, progress = percent / 100f)
                    }
                }
                refreshKey++
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                error = "Downloading ${model.name} failed: ${exception.message}"
            } finally {
                action = ModelAction.Idle
            }
        }
    }

    fun load(model: LocalModel) {
        error = null
        action = ModelAction.Loading(model)
        scope.launch {
            try {
                // Engine initialization blocks for several seconds
                withContext(Dispatchers.Default) { engine.initialize(model) }
                onModelLoaded()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                error = "Loading ${model.name} failed: ${exception.message}"
            } finally {
                action = ModelAction.Idle
            }
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        Text("Undownloaded models")
        undownloadedModels.forEach { model ->
            val downloading = (action as? ModelAction.Downloading)?.takeIf { it.model == model }
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = model.name, modifier = Modifier.weight(1f))
                    if (downloading == null) {
                        IconButton(
                            onClick = { download(model) },
                            enabled = !isBusy,
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download ${model.name}")
                        }
                    } else {
                        downloading.progress?.let { Text("${(it * 100).toInt()}%") }
                    }
                }
                if (downloading != null) {
                    val progressModifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                    val progress = downloading.progress
                    if (progress == null) {
                        LinearProgressIndicator(modifier = progressModifier)
                    } else {
                        LinearProgressIndicator(progress = { progress }, modifier = progressModifier)
                    }
                }
            }
        }

        Text("Downloaded models")
        downloadedModels.forEach { model ->
            val isLoading = (action as? ModelAction.Loading)?.model == model
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = model.name, modifier = Modifier.weight(1f))
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Button(
                            onClick = { load(model) },
                            enabled = !isBusy,
                        ) {
                            Text("Use model")
                        }
                    }
                }
            }
        }
    }
}