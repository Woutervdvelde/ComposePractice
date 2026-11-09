package com.example.composepractice.ui.project.aiaccidentdrawing.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

sealed interface DrawType {
    val label: String
    val color: Color

    data object A : DrawType {
        override val label = "Person A"
        override val color = Color.Red
    }

    data object B : DrawType {
        override val label = "Person B"
        override val color = Color.Blue
    }

    data object Road : DrawType {
        override val label = "Road"
        override val color = Color.Black
    }

    companion object {
        val entries: List<DrawType> get() = listOf(A, B, Road)
    }
}

internal data class DrawnStroke(
    val drawType: DrawType,
    val points: List<Offset>,
)

internal class SketchState {
    val strokes = mutableStateListOf<DrawnStroke>()
    var selectedDrawType by mutableStateOf<DrawType>(DrawType.A)
}

private val canvasShape = RoundedCornerShape(24.dp)

@Composable
internal fun DrawingScreen(
    sketchState: SketchState,
    onSubmit: (ImageBitmap) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val strokes = sketchState.strokes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Sketch the accident",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        SketchCanvas(
            strokes = strokes,
            selectedDrawType = sketchState.selectedDrawType,
            graphicsLayer = graphicsLayer,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DrawType.entries.forEach { drawType ->
                FilterChip(
                    selected = drawType == sketchState.selectedDrawType,
                    onClick = { sketchState.selectedDrawType = drawType },
                    label = { Text(drawType.label) },
                    leadingIcon = { ColorDot(drawType.color) },
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedIconButton(
                onClick = { strokes.removeLastOrNull() },
                enabled = strokes.isNotEmpty(),
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
            }
            OutlinedIconButton(
                onClick = { strokes.clear() },
                enabled = strokes.isNotEmpty(),
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Clear")
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { scope.launch { onSubmit(graphicsLayer.toImageBitmap()) } },
                enabled = strokes.isNotEmpty(),
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Analyze")
            }
        }
    }
}

@Composable
private fun SketchCanvas(
    strokes: MutableList<DrawnStroke>,
    selectedDrawType: DrawType,
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
) {
    val currentStroke = remember { mutableStateListOf<Offset>() }
    val drawType by rememberUpdatedState(selectedDrawType)

    Box(
        modifier = modifier
            .clip(canvasShape)
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, canvasShape),
        contentAlignment = Alignment.Center,
    ) {
        if (strokes.isEmpty() && currentStroke.isEmpty()) {
            Text(
                text = "Draw the roads and both vehicles' paths",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    fun finishStroke() {
                        if (currentStroke.isNotEmpty()) strokes.add(DrawnStroke(drawType, currentStroke.toList()))
                        currentStroke.clear()
                    }
                    detectDragGestures(
                        onDragStart = { offset -> currentStroke.add(offset) },
                        onDragEnd = ::finishStroke,
                        onDragCancel = ::finishStroke,
                        onDrag = { change, _ -> currentStroke.add(change.position) },
                    )
                },
        ) {
            graphicsLayer.record {
                drawRect(Color.White)
                (strokes + DrawnStroke(drawType, currentStroke)).forEach { (type, points) ->
                    val first = points.firstOrNull() ?: return@forEach
                    val path = Path().apply {
                        moveTo(first.x, first.y)
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path = path,
                        color = type.color,
                        style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
            if (strokes.isNotEmpty() || currentStroke.isNotEmpty()) drawLayer(graphicsLayer)
        }
    }
}

@Composable
internal fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color),
    )
}
