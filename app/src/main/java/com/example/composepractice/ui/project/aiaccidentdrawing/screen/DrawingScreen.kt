package com.example.composepractice.ui.project.aiaccidentdrawing.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.example.composepractice.ui.project.aiaccidentdrawing.AiAccidentEngine
import kotlinx.coroutines.launch

sealed interface Person {
    val label: String
    val color: Color

    data object A : Person {
        override val label = "Person A"
        override val color = Color.Red
    }

    data object B : Person {
        override val label = "Person B"
        override val color = Color.Blue
    }

    companion object {
        val entries: List<Person> get() = listOf(A, B)
    }
}

private data class DrawnStroke(
    val person: Person,
    val points: List<Offset>,
)

@Composable
internal fun DrawingScreen(engine: AiAccidentEngine) {
    var response by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val strokes = remember { mutableStateListOf<DrawnStroke>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }
    var submitted by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<Person>(Person.A) }
    
        Crossfade(
            targetState = submitted,
            modifier = Modifier.fillMaxSize()
        ) { isSubmitted ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            if (isSubmitted) {
                Text(text = response)
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(Unit) {
                            fun finishStroke() {
                                if (currentStroke.isNotEmpty()) strokes.add(DrawnStroke(selectedPerson, currentStroke.toList()))
                                currentStroke.clear()
                            }
                            detectDragGestures(
                                onDragStart = { offset -> currentStroke.add(offset) },
                                onDragEnd = ::finishStroke,
                                onDragCancel = ::finishStroke,
                                onDrag = { change, _ -> currentStroke.add(change.position) },
                            )
                        }
                ) {
                    graphicsLayer.record {
                        (strokes + DrawnStroke(selectedPerson, currentStroke)).forEach { (person, points) ->
                            val first = points.firstOrNull() ?: return@forEach
                            val path = Path().apply {
                                moveTo(first.x, first.y)
                                points.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(
                                path = path,
                                color = person.color,
                                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            )
                        }
                    }
                    drawLayer(graphicsLayer)
                }
                
                Row {
                    Person.entries.forEach {
                        FilterChip(
                            selected = it == selectedPerson,
                            label = { Text(it.label) },
                            onClick = { selectedPerson = it }
                        )
                    }
                }
                
                Row {
                    IconButton(
                        onClick = { strokes.removeLastOrNull() }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, null)
                    }

                    Button(
                        onClick = {
                            submitted = true
                            scope.launch { 
                                engine.sendImage(graphicsLayer.toImageBitmap().asAndroidBitmap()).collect { 
                                    response += it
                                }
                            }
                        }
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}